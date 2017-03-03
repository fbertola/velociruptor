package com.github.fbertola.velociruptor.processing

trait Plug<T> implements Iterator<T> {

    abstract void on()

    abstract void off()

    long getExpectedSize() {
        0l
    }

}
