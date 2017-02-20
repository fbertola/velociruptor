package com.github.fbertola.velociruptor.exceptions

import com.github.fbertola.velociruptor.processing.EventMarkerProcessor
import spock.lang.Specification

import static com.github.fbertola.velociruptor.utils.EventCreationHelper.createEvents
import static com.github.fbertola.velociruptor.utils.MetricsUtils.METRICS

class BasicExceptionHandlerTest extends Specification {

    def "should handle all exceptions"() {
        setup: "setting limits"
        def events = createEvents(1000)
        def cleaner = new EventMarkerProcessor("clean", "cleaned")
        def handler = new BasicExceptionHandler(cleaner)
        handler.maxNumberOfExceptionsLogged = 1

        when: "handling many exceptions"
        events.forEach { event -> handler.handleEventException(new NullPointerException(), 0, event) }

        then: "should have handled them all"
        def npeMeterKey = "exceptions." + NullPointerException.class.getSimpleName()
        assert METRICS.meters.containsKey(npeMeterKey)
        assert METRICS.meters.get(npeMeterKey).count == 1000

        and: "should have called the cleaner on every events"
        events.forEach { event -> assert event.payload["clean"] == "cleaned" }
    }

}
