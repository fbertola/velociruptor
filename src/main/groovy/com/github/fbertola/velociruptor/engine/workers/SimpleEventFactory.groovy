package com.github.fbertola.velociruptor.engine.workers

import com.github.fbertola.velociruptor.processing.Event
import com.lmax.disruptor.EventFactory

class SimpleEventFactory implements EventFactory<Event> {

    Event newInstance() {
        new Event()
    }
}
