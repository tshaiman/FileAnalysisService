package com.bigId.FileAnalysisService.pipeline;

import com.bigId.FileAnalysisService.Constants;
import com.bigId.FileAnalysisService.config.AppConfig;
import com.bigId.FileAnalysisService.contracts.IDataReader;
import com.bigId.FileAnalysisService.servicebus.BulkMessage;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Service
public class DataReader implements IDataReader {

    private static ObjectMapper mapper = new ObjectMapper();
    private Logger logger = LoggerFactory.getLogger(DataReader.class);

    @Autowired
    private AppConfig config;

    private int bulkSize;
    private String inputFile;
    private ServiceBus<String> sink;


    @Override
    public void start(ServiceBus<String> sinkTo) {

        this.sink = sinkTo;
        this.bulkSize = config.getBulkSize();
        this.inputFile = config.getInputSourceFile();

        logger.info("Staring Data Reader service. input file : {}, bulkSize:{}", inputFile, bulkSize);

        Executors.newSingleThreadExecutor().submit(this::fileParser);

    }

    private void fileParser() {

        List<String> bulkBuffer = new ArrayList<>(bulkSize);
        AtomicLong totalLinesRead = new AtomicLong();
        try (Stream<String> stream = Files.lines(Paths.get(inputFile))) {
            AtomicInteger bulkIndex = new AtomicInteger();
            stream.forEach(x -> {
                bulkBuffer.add(x);
                if (bulkBuffer.size() == bulkSize) {
                    flush(bulkBuffer, bulkIndex.get());
                    bulkIndex.getAndIncrement();
                }
                totalLinesRead.incrementAndGet();
            });

            if (bulkBuffer.size() > 0) {
                flush(bulkBuffer, bulkIndex.get());
            }

            logger.info("processed total of {} lines from file {}" ,totalLinesRead.get(),inputFile );

        } catch (IOException ex) {
            logger.error("could not read input file {} for parsing", inputFile, ex);
        }

        //Send EOF to all Matchers
        int maxListeners = config.getDegreeOfParallelism();
        for (int i = 0; i < maxListeners; ++i) {
            this.sink.put(Constants.EOF);
        }
    }


    private void flush(List<String> bulkBuffer, int bulkIndex) {

        try {
            BulkMessage bulk = new BulkMessage(bulkBuffer, bulkIndex);
            String json = mapper.writeValueAsString(bulk);
            sink.put(json);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        bulkBuffer.clear();
    }
}
