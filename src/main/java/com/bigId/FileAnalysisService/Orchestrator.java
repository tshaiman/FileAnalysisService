package com.bigId.FileAnalysisService;

import com.bigId.FileAnalysisService.config.AppConfig;
import com.bigId.FileAnalysisService.contracts.IAggregator;
import com.bigId.FileAnalysisService.contracts.IDataReader;
import com.bigId.FileAnalysisService.contracts.IMatcherService;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Profile("!test")
public class Orchestrator {
    private Logger logger = LoggerFactory.getLogger(Orchestrator.class);

    @Autowired
    private IDataReader dataReader;

    @Autowired
    private IMatcherService matcherService;

    @Autowired
    private IAggregator aggregator ;

    @Autowired
    private AppConfig config ;


    private ServiceBus<String> aggregatorServiceBus;
    private ServiceBus<String> readerServiceBus;

    public Orchestrator(){

    }

    @PostConstruct
    private void init() {
        readerServiceBus = new ServiceBus<>();
        aggregatorServiceBus = new ServiceBus<>();
        Executors.newSingleThreadExecutor().submit(this::runPipeline);

    }


    private void runPipeline() {
        aggregator.start(aggregatorServiceBus,config.getDegreeOfParallelism());
        matcherService.start(readerServiceBus,aggregatorServiceBus);
        dataReader.start(readerServiceBus);
        logger.info("Aggregator Service has finished");

    }


}
