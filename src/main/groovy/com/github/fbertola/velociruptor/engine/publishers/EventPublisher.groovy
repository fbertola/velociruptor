package com.github.fbertola.velociruptor.engine.publishers

import com.codahale.metrics.Meter
import com.github.fbertola.velociruptor.processing.Event
import com.github.fbertola.velociruptor.processing.Plug
import com.lmax.disruptor.RingBuffer
import groovy.util.logging.Slf4j
import lombok.NonNull

import static com.codahale.metrics.MetricRegistry.name
import static com.github.fbertola.velociruptor.utils.MetricsUtils.METRICS

@Slf4j
class EventPublisher {

    final Plug plug
    final Meter publishMeter
    final RingBuffer<Event> ringBuffer

    boolean stop
    boolean pause

    int docLogInterval = 1000

    EventPublisher(
            @NonNull Plug plug,
            @NonNull RingBuffer<Event> ringBuffer) {
        this.plug = plug
        this.ringBuffer = ringBuffer
        this.publishMeter = METRICS.meter(name(getClass(), EventPublisher.name))

        stop = false
        pause = false
    }

    void start() throws Exception {
        log.info "Publisher started, switching on the plug"
        plug.on()

        while (!isDone()) {
            if (pause) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ignore) {
                    }
                }
            } else {
                publish(plug.next())
            }
        }

        log.info "Publisher finished, switching off the plug"
        plug.off()
    }

    void pause() {
        log.info "Pausing the publisher"
        pause = true
    }

    synchronized void resume() {
        log.info "Resuming the publisher"
        pause = false
        notify()
    }

    void stop() {
        log.info "Stopping the publisher"
        pause = false
        stop = true
    }

    boolean isDone() {
        stop || plug.hasNext()
    }

    private void publish(def payload) {
        final def seq = ringBuffer.next();

        try {
            final Event eventFromRing = ringBuffer.get(seq)
            eventFromRing.payload = payload
        } finally {
            ringBuffer.publish(seq)
            publishMeter.mark()

            if (checkpoint(publishMeter.count)) {
                log.info "Published: {}/{}", publishMeter.count, plug.expectedSize
            }
        }
    }

    private def checkpoint(long currentIndex) {
        currentIndex % docLogInterval == 0l
    }

}
