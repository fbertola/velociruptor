package com.github.fbertola.velociruptor.processing

import groovy.util.logging.Slf4j

@Slf4j
class EventLoggerProcessor implements EventProcessor<Map> {

    @Override
    void process(Map object) {
        log.info object.toString()
    }

}
