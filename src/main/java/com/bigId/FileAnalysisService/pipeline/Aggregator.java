package com.bigId.FileAnalysisService.pipeline;

import com.bigId.FileAnalysisService.contracts.IAggregator;
import com.bigId.FileAnalysisService.contracts.Position;
import com.bigId.FileAnalysisService.servicebus.MatcherResult;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.*;

@Service
public class Aggregator extends ConsumerBase<MatcherResult> implements IAggregator {


    //Our global result concurrent hash map.
    // For Real production scenario : REDIS
    private ConcurrentMap<String, List<Position>> mapGlobal = new ConcurrentHashMap<>();
    private int eofGoal;
    private int eofCounter;


    @Override
    public void start(ServiceBus<String> matcherSource,int eofCounter) {
        logger.info("Staring Aggregator Service");
        this.eventBus = matcherSource;
        this.eofGoal = eofCounter;
        Executors.newSingleThreadExecutor().submit(this::listenerLoop);
    }


    private void printResults() {
        logger.info("Aggregator completed. Result Set :  ");
        mapGlobal.entrySet().stream()
            .map(kv -> new MatcherResult(kv.getKey(),kv.getValue()))
            .forEach(mr -> {
                try {
                    logger.info(mapper.writeValueAsString(mr));
                } catch (JsonProcessingException e) {
                    logger.error("could not transform matchResult to Json ",e);
                }
            });

    }


    @Override
    protected void process(MatcherResult result) {
        mapGlobal.merge(result.getName(),result.getPositions(),(v1,v2)-> {
            Set<Position> set = new TreeSet<>(v1);
            set.addAll(v2);
            return new ArrayList<>(set);
        });

    }

    @Override
    protected boolean done() {
        this.eofCounter++;
        boolean shouldQuit = this.eofCounter == this.eofGoal;
        if(shouldQuit) {
            printResults();
        }
        return shouldQuit;
    }
}
