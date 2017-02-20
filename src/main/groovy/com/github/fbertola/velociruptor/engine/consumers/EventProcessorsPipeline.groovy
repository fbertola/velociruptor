package com.github.fbertola.velociruptor.engine.consumers

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.github.fbertola.velociruptor.processing.CompositeEventProcessor
import com.github.fbertola.velociruptor.processing.EventProcessor
import groovy.util.logging.Slf4j

import static com.github.fbertola.velociruptor.utils.MetricsUtils.METRICS

@Slf4j
class EventProcessorsPipeline<T> implements AutoCloseable {

    final String name
    final int ringBufferSize
    final int concurrentWorkers

    private final Meter processed
    private final CompositeEventProcessor<T> compositeProcessors;

    EventProcessorsPipeline(
            String name,
            int ringBufferSize,
            int concurrentWorkers
    ) {
        this.name = name;
        this.ringBufferSize = ringBufferSize
        this.concurrentWorkers = concurrentWorkers;
        this.compositeProcessors = new CompositeEventProcessor()
        this.processed = METRICS.meter(MetricRegistry.name(getClass(), "processed"))

    }

    void initialize() {
        log.info "Initializing pipeline '{}'", this.name
        compositeProcessors.initialize();
    }

    void process(T object) {
        compositeProcessors.process(object)
        processed.mark()
    }

    @Override
    void close() throws Exception {
        compositeProcessors.close()
    }

    def add(EventProcessor<T> processor) {
        compositeProcessors.processors << processor
        this
    }

}
