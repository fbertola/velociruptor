package com.github.fbertola.velociruptor.engine.workers

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.processing.Event
import com.lmax.disruptor.BlockingWaitStrategy
import com.lmax.disruptor.ExceptionHandler
import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.WorkerPool
import groovy.util.logging.Slf4j
import lombok.NonNull

import java.util.concurrent.ExecutorService

@Slf4j
class EventProcessorsPipelineWorker {

    final EventProcessorsPipeline pipeline
    final WorkerPool<Event> workersPool

    final RingBuffer<Event> ringBuffer
    final ExecutorService executor
    final ExceptionHandler<Event> exceptionHandler

    EventProcessorsPipelineWorker(
            @NonNull EventProcessorsPipeline pipeline,
            @NonNull ExceptionHandler<Event> exceptionHandler,
            @NonNull ExecutorService executor
    ) {
        this.pipeline = pipeline
        this.executor = executor
        this.exceptionHandler = exceptionHandler

        ringBuffer = createRingBuffer()
        workersPool = createWorkerPool()
    }

    void stop() {
        log.info "Draining ring-buffer for pipeline '{}'", pipeline.name
        workersPool.drainAndHalt()

        log.info "Closing pipeline '{}'", pipeline.name
        pipeline.close()
    }

    void start() {
        log.info "Initializing pipeline '{}'", pipeline.name
        pipeline.initialize()

        log.info "Starting pipeline '{}'", pipeline.name
        workersPool.start(executor)

        log.info "Started pipeline '{}'", pipeline.name
    }

    private def createWorkerPool() {
        log.info "Initializing {} worker(s) for pipeline '{}' with ring buffer size of {}",
                pipeline.concurrentWorkers,
                pipeline.name,
                pipeline.ringBufferSize

        def threads = pipeline.getConcurrentWorkers()
        def workers = (1..threads).collect({ new SimpleEventWorkHandler(pipeline) }).toArray()

        def workersPool = new WorkerPool<Event>(
                ringBuffer,
                ringBuffer.newBarrier(),
                exceptionHandler,
                workers as SimpleEventWorkHandler[])


        ringBuffer.addGatingSequences(workersPool.workerSequences)

        return workersPool
    }

    private def createRingBuffer() {
        RingBuffer.createMultiProducer(
                new SimpleEventFactory(),
                pipeline.ringBufferSize,
                new BlockingWaitStrategy())
    }

}
