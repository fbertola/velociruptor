package com.github.fbertola.velociruptor.processing

class Event<T> {

    static final def END = new Event(["__last_item__", "true"])

    private T payload

    Event(T payload = null) {
        this.payload = payload
    }

    @Override
    String toString() {
        payload?.toString()
    }

    T getPayload() {
        payload
    }

    Event<T> setPayload(T payload) {
        this.payload = payload
        this
    }

    boolean isLastEvent() {
        this.payload.is END.payload
    }

}
