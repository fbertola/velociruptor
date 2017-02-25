package com.github.fbertola.velociruptor.engine.publishers

import com.codahale.metrics.Meter
import com.github.fbertola.velociruptor.processing.Event
import com.github.fbertola.velociruptor.processing.Plug
import com.lmax.disruptor.RingBuffer
import groovy.util.logging.Slf4j
import lombok.NonNull

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

import static com.codahale.metrics.MetricRegistry.name
import static com.github.fbertola.velociruptor.utils.MetricsUtils.METRICS
import static java.util.concurrent.Executors.newSingleThreadExecutor

@Slf4j
class EventPublisher {

    private final Plug plug
    private final Meter publishMeter
    private final RingBuffer<Event> ringBuffer

    private final AtomicBoolean stop
    private final AtomicBoolean pause

    private Future<?> future;

    int docLogInterval = 1000

    EventPublisher(
            @NonNull Plug plug,
            @NonNull RingBuffer<Event> ringBuffer) {
        this.plug = plug
        this.ringBuffer = ringBuffer
        this.publishMeter = METRICS.meter(name(getClass(), EventPublisher.name))
        this.stop = new AtomicBoolean(false)
        this.pause = new AtomicBoolean(false)
    }

    void start() throws Exception {
        future = newSingleThreadExecutor().submit(new InternalEventPublisher())
    }

    void pause() {
        log.info "Pausing the publisher"
        pause.set(true)
    }

    void resume() {
        log.info "Resuming the publisher"
        pause.set(false)
    }

    void stop() {
        log.info "Stopping the publisher"
        pause.set(false)
        stop.set(true)
    }

    boolean isDone() {
        future.isDone()
    }


    private class InternalEventPublisher implements Runnable {

        private final def monitor = new Object()

        void run() throws Exception {
            log.info "Publisher started, switching on the plug"
            plug.on()

            while (!isDone()) {
                if (pause.get()) {
                    synchronized (monitor) {
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

        private def isDone() {
            stop.get() || !plug.hasNext()
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

}
