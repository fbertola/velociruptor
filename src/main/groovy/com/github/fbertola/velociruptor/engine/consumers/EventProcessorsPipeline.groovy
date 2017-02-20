package com.github.fbertola.velociruptor.engine.consumers

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.github.fbertola.velociruptor.processing.CompositeEventProcessor
import com.github.fbertola.velociruptor.processing.EventProcessor
import com.github.fbertola.velociruptor.utils.MetricsUtils
import groovy.util.logging.Slf4j

@Slf4j
class EventProcessorsPipeline<T> implements AutoCloseable {

    final String name
    final Meter processed
    final int ringBufferSize
    final int concurrentWorkers
    final CompositeEventProcessor<T> compositeProcessors;

    EventProcessorsPipeline(
            String name,
            int ringBufferSize,
            int concurrentWorkers
    ) {
        this.name = name;
        this.ringBufferSize = ringBufferSize
        this.concurrentWorkers = concurrentWorkers;
        this.compositeProcessors = new CompositeEventProcessor()
        this.processed = MetricsUtils.METRICS.meter(MetricRegistry.name(getClass(), "processed"))

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
