package com.github.fbertola.velociruptor.engine.workers

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.processing.Event
import com.lmax.disruptor.WorkHandler
import lombok.NonNull

public class SimpleEventWorkHandler implements WorkHandler<Event> {

    final EventProcessorsPipeline pipeline

    public SimpleEventWorkHandler(@NonNull EventProcessorsPipeline pipeline) {
        this.pipeline = pipeline
    }

    @Override
    public void onEvent(Event event) throws Exception {
        pipeline.process(event.payload)
    }

}
