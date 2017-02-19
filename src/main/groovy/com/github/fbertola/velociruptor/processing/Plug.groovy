package com.github.fbertola.velociruptor.processing

trait Plug<T> implements Iterator<T> {

    protected int expectedSize = 0

    public abstract void on()

    public abstract void off()

}
