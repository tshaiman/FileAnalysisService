package com.bigId.FileAnalysisService.contracts;

import com.bigId.FileAnalysisService.servicebus.ServiceBus;

public interface IDataReader {
    void start(ServiceBus<String> sinkTo);
}
