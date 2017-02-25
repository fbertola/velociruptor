package com.github.fbertola.velociruptor.engine

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.engine.publishers.EventPublisher
import com.github.fbertola.velociruptor.engine.workers.EventProcessorsPipelineWorkers
import com.github.fbertola.velociruptor.exceptions.BasicExceptionHandler
import com.github.fbertola.velociruptor.processing.DoNothingEventProcessor
import com.github.fbertola.velociruptor.processing.Event
import com.github.fbertola.velociruptor.processing.Plug
import com.lmax.disruptor.ExceptionHandler
import groovy.transform.Immutable
import groovy.util.logging.Slf4j
import lombok.NonNull

import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

import static java.util.concurrent.Executors.newCachedThreadPool
import static java.util.concurrent.TimeUnit.SECONDS

@Slf4j
@Immutable
class VelociruptorStreamingEngine implements Closeable {

    @NonNull
    private Plug plug

    @NonNull
    private ExecutorService executor = newCachedThreadPool()

    @NonNull
    private ExceptionHandler<Event> exceptionHandler =
            new BasicExceptionHandler(new DoNothingEventProcessor())

    private int docLogInterval = 1000
    private int waitForWorkersSleepTime = 10

    private final List<EventProcessorsPipeline> pipelines = [];
    private final AtomicBoolean orderStop = new AtomicBoolean(false)


    VelociruptorStreamingEngine addPipeline(EventProcessorsPipeline pipeline) {
        pipelines.add(pipeline)
        this
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
        publisher.isDone() || orderStop.get()
    }

}
