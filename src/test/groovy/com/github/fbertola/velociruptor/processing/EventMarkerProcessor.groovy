package com.github.fbertola.velociruptor.processing

import groovy.util.logging.Slf4j

@Slf4j
class EventMarkerProcessor implements EventProcessor<Map> {

    private final String field
    private final String markValue

    EventMarkerProcessor(String field, String markValue) {
        this.field = field
        this.markValue = markValue
    }

    @Override
    void process(Map map) {
        map[field] = markValue
    }

    @Override
    boolean accept(Map map) {
        true
    }

    @Override
    public void initialize() {
        log.info "Will mark items on field '{}' with value '{}'", field, markValue
    }

    @Override
    public void close() {
        log.info "Closing {}", getClass().name
    }

}
