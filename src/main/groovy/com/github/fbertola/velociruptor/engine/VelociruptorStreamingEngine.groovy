package com.github.fbertola.velociruptor.engine

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.engine.publishers.EventPublisher
import com.github.fbertola.velociruptor.engine.workers.EventProcessorsPipelineWorkers
import com.github.fbertola.velociruptor.processing.Event
import com.github.fbertola.velociruptor.processing.Plug
import com.lmax.disruptor.ExceptionHandler
import groovy.util.logging.Slf4j
import lombok.NonNull

import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

import static java.util.concurrent.TimeUnit.SECONDS

@Slf4j
class VelociruptorStreamingEngine {

    final Plug plug
    final ExecutorService executor
    final AtomicBoolean orderStop
    final List<EventProcessorsPipeline> pipelines;
    final ExceptionHandler<Event> exceptionHandler

    int docLogInterval = 1000
    int waitForWorkersSleepTime = 10

    public VelociruptorStreamingEngine(
            @NonNull Plug plug,
            @NonNull ExecutorService executor,
            @NonNull ExceptionHandler<Event> exceptionHandler) {
        this.plug = plug
        this.executor = executor
        this.exceptionHandler = exceptionHandler
        this.pipelines = [];

        orderStop = new AtomicBoolean(false)
    }

    public void process() {
        log.info "Start processing"

        def workers = new EventProcessorsPipelineWorkers(pipelines, executor, exceptionHandler);
        def publisher = new EventPublisher(plug, workers.ringBuffer)

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

    private def isTimeToStop(EventPublisher publisher) {
        return publisher.isDone() || orderStop.get()
    }

}
