package com.bigId.FileAnalysisService.pipeline;


import com.bigId.FileAnalysisService.Constants;
import com.bigId.FileAnalysisService.config.AppConfig;
import com.bigId.FileAnalysisService.contracts.IMatcherService;
import com.bigId.FileAnalysisService.contracts.Position;
import com.bigId.FileAnalysisService.servicebus.BulkMessage;
import com.bigId.FileAnalysisService.servicebus.MatcherResult;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A Class to coordinate all the matching workers
 */
@Service
public class MatcherService extends ConsumerBase<BulkMessage> implements IMatcherService {


    private static ObjectMapper mapper = new ObjectMapper();

    private List<String> lookups;
    @Setter
    private ServiceBus<String> sink;


    @Autowired
    private AppConfig config;

    @PostConstruct
    void postConstruct() {
        this.lookups = config.getLookups();
    }

    @Override
    public void start(ServiceBus<String> source, ServiceBus<String> sinkTo) {
        this.eventBus = source;
        this.sink = sinkTo;


        int degreeOfParallelism = config.getDegreeOfParallelism();
        logger.info("Starting Matcher Service Processors, degreeOfParallelism = {} .",degreeOfParallelism);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(degreeOfParallelism);
        IntStream.rangeClosed(0, degreeOfParallelism)
                .forEach(i -> executor.submit(this::listenerLoop));

    }


    @Override
    protected void process(BulkMessage bulk) {
        int startLine = bulk.getBulkOffset() * config.getBulkSize();
        List<String> lines = bulk.getLines();
        int size = lines.size();

        for (int i = 0; i < size; ++i) {
            List<MatcherResult> results  = processLine(lines.get(i), i + 1 + startLine);
            results.forEach(this::sendBulkResult);
        }
    }

    List<MatcherResult> processLine(String line, int lineNumber) {
        return reduce(map(line), lineNumber);
    }

    /**
     * we are doing a SINGLE pass on the line , this is the "map" phase .
     * This will increase performance significantly -> O(n)
     * @param line - a line from the original text
     * @return maping of word locations
     */
    private HashMap<String, List<Integer>> map(String line) {
        Matcher matcher = Constants.pattern.matcher(line);
        HashMap<String, List<Integer>> hashMap = new HashMap<>();

        while (matcher.find()) {
            MatchResult mr = matcher.toMatchResult();
            hashMap.merge(mr.group(),
                    new ArrayList<>(Collections.singletonList(matcher.start())),
                    (lst, cur) -> {
                        lst.add(matcher.start());
                        return lst;
                    });
        }
        return hashMap;
    }

    /**
     * Now that we have all the words locations in the line we can match it against the names in O(1)
     * @param indexes - map words locations
     * @param lineNumber - the index of the line within the file
     * @return list of matching results
     */
    private List<MatcherResult> reduce(HashMap<String, List<Integer>> indexes, int lineNumber) {
        return lookups
                .stream()
                .map(name -> toMatcherResult(name, indexes, lineNumber))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    private MatcherResult toMatcherResult(String name, HashMap<String, List<Integer>> indexes, int lineNumber) {
        if (indexes.containsKey(name)) {
            List<Position> positions =
                    indexes
                    .get(name)
                    .stream()
                    .map(index -> new Position(lineNumber, index))
                    .collect(Collectors.toList());

            return new MatcherResult(name, positions);
        }
        return null;
    }

    private void sendBulkResult(MatcherResult matcherResult) {
        try {
            String matchResult = mapper.writeValueAsString(matcherResult);
            sink.put(matchResult);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean done() {
        logger.info("Matcher Service Completed Successfully.");
        sink.put(Constants.EOF);
        return true;
    }



}
