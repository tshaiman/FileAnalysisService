package com.bigId.FileAnalysisService.config;

//import com.bigId.FileAnalysisService.servicebus.IServiceBus;
//import com.bigId.FileAnalysisService.servicebus.KafkaServiceBus;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.ws.Service;

@Configuration
public class FileAnalysisConfiguration {

//    @Bean
//    @ConditionalOnProperty(name = "servicebus.type", havingValue = "standalone", matchIfMissing = true)
//    public IServiceBus createStandaloneServiceBus() {
//        return new ServiceBus();
//    }
//
//    @Bean
//    @ConditionalOnProperty(name = "servicebus.type", havingValue = "kafka", matchIfMissing = true)
//    public IServiceBus createKafkaServiceBus() {
//        return new KafkaServiceBus();
//    }
}
