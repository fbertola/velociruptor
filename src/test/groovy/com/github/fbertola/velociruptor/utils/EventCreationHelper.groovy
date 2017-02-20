package com.github.fbertola.velociruptor.utils

import com.github.fbertola.velociruptor.processing.Event

class EventCreationHelper {

    static def createEvents(int numberOfItems, int offset = 0) {
        def events = []

        def from = offset, to = (numberOfItems + offset)

        (from..<to).forEach { i ->
            events.add new Event(createPayload(i))
        }

        events as List<Event>
    }


    private static def createPayload(index) {
        [
                "index": index,
                "name" : "createdEvent"
        ]
    }

}
