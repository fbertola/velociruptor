package com.github.fbertola.velociruptor.processing


class CompositeEventProcessor<T> implements EventProcessor<T> {

    final List<EventProcessor<T>> processors

    CompositeEventProcessor() {
        processors = []
    }

    @Override
    void initialize() {
        processors*.initialize()
    }

    @Override
    void process(T object) {
        processors.forEach { p -> if (p.accepts(object)) p.process(object) }
    }

    @Override
    void close() {
        processors*.close()
    }

}
