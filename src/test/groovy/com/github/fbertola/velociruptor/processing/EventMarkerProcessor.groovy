package com.github.fbertola.velociruptor.processing

import groovy.util.logging.Slf4j

import java.util.concurrent.atomic.LongAdder

@Slf4j
class EventMarkerProcessor implements EventProcessor<Map> {

    private final String field
    private final String markValue
    private final LongAdder _processed

    EventMarkerProcessor(
            String field = "mark",
            String markValue = "marked") {
        this.field = field
        this.markValue = markValue
        this._processed = new LongAdder()
    }

    @Override
    void process(Map map) {
        _processed.increment()
        map[field] = markValue
    }

    @Override
    boolean accepts(Map map) {
        true
    }

    @Override
    void initialize() {
        log.info "Will mark items on field '{}' with value '{}'", field, markValue
    }

    @Override
    void close() {
        log.info "Closing '{}'", getClass().name
    }

    long getProcessed() {
        _processed.longValue()
    }

}
