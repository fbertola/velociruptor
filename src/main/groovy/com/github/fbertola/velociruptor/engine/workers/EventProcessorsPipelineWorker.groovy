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

    private final RingBuffer<Event> intake
    private final RingBuffer<Event> outtake
    private final EventProcessorsPipeline pipeline
    private final WorkerPool<Event> workersPool
    private final ExecutorService executor
    private final ExceptionHandler<Event> exceptionHandler

    EventProcessorsPipelineWorker(
            @NonNull EventProcessorsPipeline pipeline,
            @NonNull ExceptionHandler<Event> exceptionHandler,
            @NonNull ExecutorService executor,
            @NonNull RingBuffer<Event> intake,
            RingBuffer<Event> outtake
    ) {
        this.pipeline = pipeline
        this.executor = executor
        this.exceptionHandler = exceptionHandler
        this.intake = intake
        this.outtake = outtake

        workersPool = createWorkerPool()
    }

    void stop() {
        def pipelineName = pipeline.name

        log.info "Draining ring-buffer for pipeline '{}'", pipelineName
        workersPool.drainAndHalt()

        log.info "Closing pipeline '{}'", pipelineName
        pipeline.close()
    }

    void start() {
        def pipelineName = pipeline.name

        log.info "Initializing pipeline '{}'", pipelineName
        pipeline.initialize()

        log.info "Starting pipeline '{}'", pipelineName
        workersPool.start(executor)

        log.info "Started pipeline '{}'", pipelineName
    }

    private def createWorkerPool() {
        log.info "Initializing {} worker(s) for pipeline '{}' with ring buffer size of {}",
                pipeline.concurrentWorkers,
                pipeline.name,
                pipeline.ringBufferSize

        def threads = pipeline.concurrentWorkers
        def workers = (1..threads).collect({
            new SimpleEventWorkHandler(pipeline, outtake)
        }).toArray()

        def workersPool = new WorkerPool<Event>(
                intake,
                intake.newBarrier(),
                exceptionHandler,
                workers as SimpleEventWorkHandler[])

        intake.addGatingSequences(workersPool.workerSequences)

        return workersPool
    }

}
