package com.github.fbertola.velociruptor.engine.workers

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.processing.Event
import com.lmax.disruptor.RingBuffer
import com.lmax.disruptor.WorkHandler

public class CachedEventWorkHandler implements WorkHandler<Event> {

    private final EventProcessorsPipeline pipeline
    private final RingBuffer<Event> ringBuffer

    CachedEventWorkHandler(
            EventProcessorsPipeline pipeline,
            RingBuffer<Event> ringBuffer) {
        this.pipeline = pipeline
        this.ringBuffer = ringBuffer
    }

    @Override
    void onEvent(Event event) throws Exception {
        pipeline.process(event.payload)
        writeOnRingBuffer(event)
    }


    private void writeOnRingBuffer(event) {
        if (ringBuffer == null) return

        def seq = ringBuffer.next()

        try {
            ringBuffer.get(seq).setPayload(event.payload)
        } finally {
            ringBuffer.publish(seq)
        }
    }

}
