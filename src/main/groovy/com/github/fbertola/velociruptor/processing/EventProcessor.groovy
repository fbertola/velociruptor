package com.github.fbertola.velociruptor.processing

import groovy.util.logging.Slf4j

@Slf4j
trait EventProcessor<T> {

    void initialize() {}

    void process(T object) {}

    void close() {
        log.info "Closing {}", getClass()
    }

    public boolean accept(T object) {
        return true;
    }

}