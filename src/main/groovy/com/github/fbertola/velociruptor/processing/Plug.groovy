package com.github.fbertola.velociruptor.processing

trait Plug<T> implements Iterator<T> {

    int expectedSize = 0

    abstract void on()

    abstract void off()

}
