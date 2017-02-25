package com.github.fbertola.velociruptor.processing

trait Plug<T> implements Iterator<T> {

    long expectedSize = 0

    abstract void on()

    abstract void off()

}
