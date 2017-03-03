package com.github.fbertola.velociruptor.engine.workers

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.processing.Event
import com.lmax.disruptor.ExceptionHandler
import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.WorkerPool
import groovy.util.logging.Slf4j

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
            EventProcessorsPipeline pipeline,
            ExceptionHandler<Event> exceptionHandler,
            ExecutorService executor,
            RingBuffer<Event> intake,
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
            new CachedEventWorkHandler(pipeline, outtake)
        }).toArray()

        def workersPool = new WorkerPool<Event>(
                intake,
                intake.newBarrier(),
                exceptionHandler,
                workers as CachedEventWorkHandler[])

        intake.addGatingSequences(workersPool.workerSequences)

        return workersPool
    }

}
