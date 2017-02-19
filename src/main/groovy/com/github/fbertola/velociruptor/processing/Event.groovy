package com.github.fbertola.velociruptor.processing

class Event<T> {

    static final def WAIT = new Event()
    static final def NULL = new Event()
    static final def ERROR = new Event()

    T payload

}
