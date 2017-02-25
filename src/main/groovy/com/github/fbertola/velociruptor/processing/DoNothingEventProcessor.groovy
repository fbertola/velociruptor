package com.github.fbertola.velociruptor.processing

class DoNothingEventProcessor<T> implements EventProcessor<T> {

    void process(T object) {
        void // Do nothing
    }

    @Override
    boolean accepts(T object) {
        return false
    }

}
