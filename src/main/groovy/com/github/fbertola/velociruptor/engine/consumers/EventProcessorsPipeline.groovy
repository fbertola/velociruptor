package com.github.fbertola.velociruptor.engine.consumers

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.github.fbertola.velociruptor.processing.CompositeEventProcessor
import com.github.fbertola.velociruptor.processing.EventProcessor
import groovy.transform.Immutable
import groovy.util.logging.Slf4j

import static com.github.fbertola.velociruptor.utils.MetricsUtils.METRICS

@Slf4j
@Immutable
class EventProcessorsPipeline<T> implements AutoCloseable {

    int concurrentWorkers = 1
    int ringBufferSize = 1024
    String name = "unnamed pipeline"

    private final Meter processed = METRICS.meter(MetricRegistry.name(getClass(), "processed"))
    private final CompositeEventProcessor<T> compositeProcessors = new CompositeEventProcessor<T>()

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

    EventProcessorsPipeline<T> add(EventProcessor<T> processor) {
        compositeProcessors.processors << processor
        this
    }

}
