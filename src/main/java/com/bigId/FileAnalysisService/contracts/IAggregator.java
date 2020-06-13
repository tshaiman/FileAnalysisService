package com.bigId.FileAnalysisService.contracts;

import com.bigId.FileAnalysisService.servicebus.ServiceBus;

import java.util.concurrent.CountDownLatch;

public interface IAggregator {

    void start(ServiceBus<String> matcherSource,int eofCounter);


}
