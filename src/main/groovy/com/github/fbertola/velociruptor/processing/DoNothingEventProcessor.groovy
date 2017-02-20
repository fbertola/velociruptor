package com.github.fbertola.velociruptor.processing

class DoNothingEventProcessor<T> implements EventProcessor<T> {

    void process(T object) {
        // Do nothing
    }

    @Override
    boolean accept(T object) {
        return false
    }

}
