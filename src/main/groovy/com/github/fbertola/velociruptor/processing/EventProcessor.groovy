package com.github.fbertola.velociruptor.processing

import groovy.util.logging.Slf4j

@Slf4j
trait EventProcessor<T> {

    void initialize() {}

    abstract void process(T object)

    void close() {
        log.info "Closing {}", getClass()
    }

    boolean accept(T object) {
        return true;
    }

    @Override
    String toString() {
        getClass().name
    }

}