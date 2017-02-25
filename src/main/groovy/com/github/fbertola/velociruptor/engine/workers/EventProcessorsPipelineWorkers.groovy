package com.github.fbertola.velociruptor.engine.workers

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.processing.Event
import com.lmax.disruptor.BlockingWaitStrategy
import com.lmax.disruptor.ExceptionHandler
import com.lmax.disruptor.RingBuffer
import groovy.util.logging.Slf4j
import lombok.NonNull

import java.util.concurrent.ExecutorService

@Slf4j
class EventProcessorsPipelineWorkers {

    private final List<EventProcessorsPipelineWorker> workers = []

    private RingBuffer<Event> firstRingBuffer = null

    EventProcessorsPipelineWorkers(
            @NonNull List<EventProcessorsPipeline> pipelines,
            @NonNull ExecutorService executor,
            @NonNull ExceptionHandler<Event> exceptionHandler) {

        log.info "Creating pipeline workers"

        RingBuffer<Event> previousRingBuffer = null

        // wiring the 'in' and 'out' RingBuffers in reverse order
        pipelines.reverse().forEach { pipeline ->
            log.info "Creating worker for pipeline '{}'", pipeline.name
            def currentRingBuffer = createRingBuffer(pipeline.ringBufferSize)
            def worker = new EventProcessorsPipelineWorker(
                    pipeline,
                    exceptionHandler,
                    executor,
                    currentRingBuffer,
                    previousRingBuffer)

            workers.add(0, worker) // reversed order
            firstRingBuffer = currentRingBuffer
            previousRingBuffer = currentRingBuffer
        }
    }

    void stop() {
        workers*.stop()
    }

    void start() {
        workers*.start()
    }

    def getRingBuffer() {
        firstRingBuffer
    }

    private static def createRingBuffer(int size) {
        RingBuffer.createMultiProducer(
                new SimpleEventFactory(),
                size,
                new BlockingWaitStrategy())
    }
}
