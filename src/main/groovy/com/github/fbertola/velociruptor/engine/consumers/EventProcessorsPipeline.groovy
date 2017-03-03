package com.github.fbertola.velociruptor.engine.consumers

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.github.fbertola.velociruptor.processing.EventProcessor
import groovy.util.logging.Slf4j

import static com.github.fbertola.velociruptor.utils.MetricsUtils.METRICS

@Slf4j
class EventProcessorsPipeline<T> implements AutoCloseable {

    private int concurrentWorkers = 1
    private int ringBufferSize = 1024
    private String name = "unnamed pipeline"

    private final Meter processed
    private final List<EventProcessor<T>> processors

    EventProcessorsPipeline() {
        processors = []
        processed = METRICS.meter(MetricRegistry.name(getClass(), "processed"))
    }

    void initialize() {
        log.info "Initializing pipeline '{}'", this.name
        processors*.initialize()
    }

    void process(T object) {
        processors*.process(object)
        processed.mark()
    }

    @Override
    void close() throws Exception {
        processors*.close()
    }

    EventProcessorsPipeline<T> add(EventProcessor<T> processor) {
        processors << processor
        this
    }

    int getConcurrentWorkers() {
        concurrentWorkers
    }

    int getRingBufferSize() {
        ringBufferSize
    }

    String getName() {
        name
    }

    EventProcessorsPipeline<T> setConcurrentWorkers(int concurrentWorkers) {
        assert concurrentWorkers > 0

        this.concurrentWorkers = concurrentWorkers
        this
    }

    EventProcessorsPipeline<T> setRingBufferSize(int ringBufferSize) {
        assert ringBufferSize > 0

        this.ringBufferSize = ringBufferSize
        this
    }

    EventProcessorsPipeline<T> setName(String name) {
        assert name != null

        this.name = name
        this
    }

}
