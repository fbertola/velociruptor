package com.github.fbertola.velociruptor.processing

class ProgrammablePlug implements Plug<Map> {

    private final long numberOfObjects

    private long served = 0

    ProgrammablePlug(long numberOfObjects) {
        this.numberOfObjects = numberOfObjects
    }

    @Override
    void on() {

    }

    @Override
    void off() {

    }

    @Override
    boolean hasNext() {
        served < numberOfObjects
    }

    @Override
    Map next() {
        ["index": served++]
    }

    @Override
    long getExpectedSize() {
        numberOfObjects
    }

}
