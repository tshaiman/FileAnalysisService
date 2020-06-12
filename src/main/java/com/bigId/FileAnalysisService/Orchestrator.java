package com.bigId.FileAnalysisService;

import com.bigId.FileAnalysisService.config.AppConfig;
import com.bigId.FileAnalysisService.contracts.IAggregator;
import com.bigId.FileAnalysisService.contracts.IDataReader;
import com.bigId.FileAnalysisService.contracts.IMatcherService;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
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

    private CountDownLatch completedMatching ;
    private CountDownLatch completeAggregation;

    ServiceBus<String> aggregatorServiceBus;

    public Orchestrator(){

    }

    public void run() {
        completedMatching = new CountDownLatch(config.getDegreeOfParallelism());
        completeAggregation = new CountDownLatch(1);

        //Building Service Buses
        ServiceBus<String> inputBulks = new ServiceBus<String>();
        aggregatorServiceBus = new ServiceBus<>();

        //Graph Building
        aggregator.start(aggregatorServiceBus,completeAggregation);
        matcherService.start(inputBulks,aggregatorServiceBus,completedMatching);
        dataReader.start(inputBulks);

        Executors.newSingleThreadExecutor().submit(this::waitAndPrintResults);

    }

    private void waitAndPrintResults() {
        try {
            boolean completed = completedMatching.await(10, TimeUnit.MINUTES);
            if(!completed) {
                logger.warn("FileAnalysis has been timed out. check logs");
                return;
            }
            logger.info("FileAnalysis completed. Result Set :  ");
            aggregatorServiceBus.put(Constants.EOF);

            completeAggregation.await(10,TimeUnit.SECONDS);
            aggregator.printResults();


        } catch (InterruptedException e) {
            logger.error("FileAnalysis graph failed",e);
        }


    }

}
