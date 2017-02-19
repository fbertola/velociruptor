package com.github.fbertola.velociruptor.engine.workers

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.processing.Event
import com.lmax.disruptor.ExceptionHandler
import groovy.util.logging.Slf4j
import lombok.NonNull

import java.util.concurrent.ExecutorService

@Slf4j
class EventProcessorsPipelineWorkers {

    private final List<EventProcessorsPipelineWorker> workers = []

    public EventProcessorsPipelineWorkers(
            @NonNull List<EventProcessorsPipeline> pipelines,
            @NonNull ExecutorService executor,
            @NonNull ExceptionHandler<Event> exceptionHandler) {

        log.info "Creating pipeline workers"

        pipelines.reverse().forEach { pipeline ->
            log.info "Creating worker for pipeline '{}'", pipeline.name
            def worker = new EventProcessorsPipelineWorker(pipeline, exceptionHandler, executor)

            workers.add 0, worker // reverse order
        }
    }

    public void stop() {
        workers*.stop()
    }

    public void start() {
        workers*.start()
    }

    public def getRingBuffer() {
        return workers[0].ringBuffer
    }
}
