package com.github.fbertola.velociruptor.engine.workers

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.processing.Event
import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.WorkHandler
import lombok.NonNull

public class SimpleEventWorkHandler implements WorkHandler<Event> {

    private final EventProcessorsPipeline pipeline
    private final RingBuffer<Event> ringBuffer

    SimpleEventWorkHandler(
            @NonNull EventProcessorsPipeline pipeline,
            RingBuffer<Event> ringBuffer) {
        this.pipeline = pipeline
        this.ringBuffer = ringBuffer
    }

    @Override
    void onEvent(Event event) throws Exception {
        pipeline.process(event.payload)

        if (ringBuffer != null) {
            writeOnRingBuffer(event)
        }
    }

    private void writeOnRingBuffer(Event event) {
        def seq = ringBuffer.next()

        try {
            def eventFromRing = ringBuffer.get(seq)
            eventFromRing.setPayload(event.payload)
        } finally {
            ringBuffer.publish(seq)
        }
    }

}
