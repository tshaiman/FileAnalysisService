package com.bigId.FileAnalysisService.pipeline;

import com.bigId.FileAnalysisService.Constants;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;

import java.io.IOException;

public abstract class ConsumerBase<T>{

    final Class<T> typeParameterClass;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected ServiceBus<String> eventBus;
    protected ObjectMapper mapper = new ObjectMapper();

    public ConsumerBase() {
        this.typeParameterClass = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), ConsumerBase.class);
    }

    /**
     * Generic Implementation of Event Loop handling from Service Bus
     */
    protected void listenerLoop() {
        while (true) {
            try {
                String msg = eventBus.poll(500);
                if (msg != null) {

                    if (msg.equals(Constants.EOF)){
                        logger.info("{} received EOF processing event. exiting ",this.getClass().getName());
                        done();
                    }
                    else {
                        T data = mapper.readValue(msg, typeParameterClass);
                        process(data);
                    }
                }
            } catch (IOException ex) {
                logger.error("{} Consumer has failed. ",this.getClass().getName(),ex);
            }
        }
    }

    protected abstract void process(T data);
    protected abstract void done();
}
