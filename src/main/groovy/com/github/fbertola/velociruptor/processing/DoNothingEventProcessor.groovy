package com.github.fbertola.velociruptor.processing

class DoNothingEventProcessor<T> implements EventProcessor<T> {

    void process(T object) {
        void //NOSONAR
    }

    @Override
    boolean accepts(T object) {
        false //NOSONAR
    }

}
