package com.github.fbertola.velociruptor.engine

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.engine.publishers.EventPublisher
import com.github.fbertola.velociruptor.engine.workers.EventProcessorsPipelineWorkers
import com.github.fbertola.velociruptor.exceptions.BasicExceptionHandler
import com.github.fbertola.velociruptor.processing.DoNothingEventProcessor
import com.github.fbertola.velociruptor.processing.Event
import com.github.fbertola.velociruptor.processing.Plug
import com.lmax.disruptor.ExceptionHandler
import groovy.util.logging.Slf4j
import lombok.NonNull

import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

import static java.util.concurrent.TimeUnit.SECONDS

@Slf4j
class VelociruptorStreamingEngine implements Closeable {

    private final Plug plug
    private final ExecutorService executor
    private final AtomicBoolean orderStop
    private final List<EventProcessorsPipeline> pipelines;
    private final ExceptionHandler<Event> exceptionHandler

    int docLogInterval = 1000
    int waitForWorkersSleepTime = 10

    VelociruptorStreamingEngine(
            @NonNull Plug plug,
            @NonNull ExecutorService executor) {
        this(plug, executor, new BasicExceptionHandler(new DoNothingEventProcessor()))
    }

    VelociruptorStreamingEngine(
            @NonNull Plug plug,
            @NonNull ExecutorService executor,
            @NonNull ExceptionHandler<Event> exceptionHandler) {
        this.plug = plug
        this.executor = executor
        this.exceptionHandler = exceptionHandler
        this.pipelines = [];

        orderStop = new AtomicBoolean(false)
    }

    void process() {
        log.info "Start processing"

        def workers = new EventProcessorsPipelineWorkers(pipelines, executor, exceptionHandler);
        def publisher = new EventPublisher(plug, workers.ringBuffer)

        publisher.docLogInterval = docLogInterval

        workers.start();
        publisher.start()

        while (!isTimeToStop(publisher)) {
            SECONDS.sleep(waitForWorkersSleepTime)
        }

        log.info "Stopping the event publisher"
        publisher.stop()
        log.info "Event publisher stopped"

        log.info "Stopping the workers"
        workers.stop()
        log.info "Workers stopped"

        log.info "All done. The engine exited successfully"
    }

    @Override
    void close() {
        log.info "Close called, stopping the engine"
        orderStop.set(true);
    }

    private def isTimeToStop(EventPublisher publisher) {
        return publisher.isDone() || orderStop.get()
    }

}
