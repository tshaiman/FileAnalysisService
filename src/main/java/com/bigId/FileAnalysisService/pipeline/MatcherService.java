package com.bigId.FileAnalysisService.pipeline;


import com.bigId.FileAnalysisService.Constants;
import com.bigId.FileAnalysisService.config.AppConfig;
import com.bigId.FileAnalysisService.contracts.IMatcherService;
import com.bigId.FileAnalysisService.servicebus.BulkMessage;
import com.bigId.FileAnalysisService.servicebus.MatcherResult;
import com.bigId.FileAnalysisService.model.Position;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A Class to coordinate all the matching workers
 */
@Service
public class MatcherService implements IMatcherService {

    private static Pattern pattern = Pattern.compile("\\w+");
    private static ObjectMapper mapper = new ObjectMapper();
    private List<String> lookups;
    private ServiceBus<String> source;
    private ServiceBus<String> sink;
    private ThreadPoolExecutor executor;
    @Autowired
    private AppConfig config;


    public MatcherService() {
    }

    @Override
    public void start(ServiceBus<String> source, ServiceBus<String> sinkTo) {
        this.source = source;
        this.sink = sinkTo;
        this.lookups = config.getLookups();
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.getDegreeOfParallelism());
        for (int i = 0; i < config.getDegreeOfParallelism(); i++) {
            int finalI = i;
            this.executor.submit(()-> startMatcherTask(finalI));
        }
    }

    private void startMatcherTask(int workerId) {
        while (true) {
            try {
                String msg = source.poll(500);
                if (msg != null) {

                    if (msg.equals(Constants.EOF)){
                        System.out.printf("received End of processing request, workerId = %d. exiting%n" , workerId );
                        break;
                    }
                    else {
                        BulkMessage bulk = mapper.readValue(msg, BulkMessage.class);
                        processBulk(bulk);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    private void processBulk(BulkMessage bd) {
        int startLine = bd.getBulkOffset() * 1000;
        List<String> lines = bd.getLines();
        int size = lines.size();

        for (int i = 0; i < size; ++i) {
            List<MatcherResult> bulkResults = processLine(lines.get(i), i + 1 + startLine);
            bulkResults.forEach(this::sendBulkResult);
        }
    }

    private List<MatcherResult> processLine(String line, int lineNumber) {
        return reduce(map(line), lineNumber);
    }

    /**
     * we are doing a SINGLE pass on the line , this is the "map" phase .
     * This will increase performance significantly -> O(n)
     * @param line - a line from the original text
     * @return maping of word locations
     */
    private HashMap<String, List<Integer>> map(String line) {
        Matcher matcher = pattern.matcher(line);
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



}
