package com.bigId.FileAnalysisService.contracts;

import com.bigId.FileAnalysisService.servicebus.ServiceBus;

import java.util.concurrent.CountDownLatch;

public interface IMatcherService {
    void start(ServiceBus<String> source, ServiceBus<String> sinkTo);
}
