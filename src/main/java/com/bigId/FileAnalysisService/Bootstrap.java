package com.bigId.FileAnalysisService;

import com.bigId.FileAnalysisService.contracts.IDataReader;
import com.bigId.FileAnalysisService.contracts.IMatcherService;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Bootstrap {

    @Autowired
    private IDataReader dataReader;

    @Autowired
    private IMatcherService matcherService;

    public Bootstrap(){

    }

    public void build() {
        //Build Service Bus queues
        ServiceBus<String> inputBulks = new ServiceBus<String>();
        ServiceBus<String> matchingResults = new ServiceBus<>();

        matcherService.start(inputBulks,matchingResults);
        dataReader.start(inputBulks);

    }
}
