package com.github.fbertola.velociruptor.exceptions

import com.github.fbertola.velociruptor.processing.Event
import com.github.fbertola.velociruptor.processing.EventProcessor
import com.lmax.disruptor.ExceptionHandler
import groovy.util.logging.Slf4j

import static com.codahale.metrics.MetricRegistry.name
import static com.github.fbertola.velociruptor.utils.MetricsUtils.METRICS

@Slf4j
class BasicExceptionHandler implements ExceptionHandler<Event> {

    private final EventProcessor eventProcessor;

    private int maxNumberOfExceptionsLogged;
    private int intervalOfExceptionLogged;

    BasicExceptionHandler(EventProcessor eventProcessor) {
        log.info "Exception event processor is '{}'", eventProcessor

        this.eventProcessor = eventProcessor
        maxNumberOfExceptionsLogged = 100
        intervalOfExceptionLogged = 1000
    }

    @Override
    void handleEventException(Throwable ex, long sequence, Event event) {
        def exceptionMeter = METRICS.meter(name("exceptions", ex.getClass().getSimpleName()))
        exceptionMeter.mark()

        try {
            def exceptionCount = exceptionMeter.count

            if (exceptionCount < maxNumberOfExceptionsLogged ||
                    exceptionCount % intervalOfExceptionLogged == 0l) {
                log.error "Exception processing ${sequence}, event '${event}'", ex
            }

            def payload = event.payload

            if (eventProcessor.accepts(payload)) {
                eventProcessor.process(payload)
            }
        } catch (Exception e) {
            log.error "Unable to handle exception", e
        }

    }

    @Override
    void handleOnStartException(Throwable ex) {
        log.error "Exception during onStart()", ex
    }

    @Override
    void handleOnShutdownException(Throwable ex) {
        log.error "Exception during onShutdown()", ex
    }

    int getMaxNumberOfExceptionsLogged() {
        maxNumberOfExceptionsLogged
    }

    BasicExceptionHandler setMaxNumberOfExceptionsLogged(int maxNumberOfExceptionsLogged) {
        this.maxNumberOfExceptionsLogged = maxNumberOfExceptionsLogged
        this
    }

    int getIntervalOfExceptionLogged() {
        intervalOfExceptionLogged
    }

    BasicExceptionHandler setIntervalOfExceptionLogged(int intervalOfExceptionLogged) {
        this.intervalOfExceptionLogged = intervalOfExceptionLogged
        this
    }
}
