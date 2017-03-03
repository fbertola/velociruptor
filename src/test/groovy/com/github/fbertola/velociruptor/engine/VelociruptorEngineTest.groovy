package com.github.fbertola.velociruptor.engine

import com.github.fbertola.velociruptor.engine.consumers.EventProcessorsPipeline
import com.github.fbertola.velociruptor.processing.EventMarkerProcessor
import com.github.fbertola.velociruptor.processing.ProgrammablePlug
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.ExecutorService

import static java.util.concurrent.Executors.newCachedThreadPool

class VelociruptorEngineTest extends Specification {

    @Shared
    private ExecutorService executor;

    void setupSpec() {
        executor = newCachedThreadPool();
    }

    void "should process max number of documents"() {
        setup: "engine creation with dummy pipelines"
        def plug = new ProgrammablePlug(5000l)
        def processor1 = new EventMarkerProcessor()
        def processor2 = new EventMarkerProcessor()

        def pipeline1 = new EventProcessorsPipeline<Map>()
                .setName("pipeline1")
                .setConcurrentWorkers(4)
                .setRingBufferSize(512)
                .add(processor1)

        def pipeline2 = new EventProcessorsPipeline<Map>()
                .setName("pipeline2")
                .setConcurrentWorkers(2)
                .setRingBufferSize(256)
                .add(processor2)

        def engine = new VelociruptorEngine(plug, executor)
                .setWaitForWorkersSleepTime(1)
                .addPipeline(pipeline1)
                .addPipeline(pipeline2)

        when: "the engine runs"
        engine.process()

        then: "every event is processed by the 2 pipelines"
        assert processor1.processed == 5000l
        assert processor2.processed == 5000l
    }

}
