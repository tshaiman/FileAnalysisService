package com.bigId.FileAnalysisService.pipeline;

import com.bigId.FileAnalysisService.config.AppConfig;
import com.bigId.FileAnalysisService.contracts.IDataReader;
import com.bigId.FileAnalysisService.servicebus.BulkMessage;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class DataReader implements IDataReader {

    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private AppConfig config;

    private int bulkSize;
    private ServiceBus<String> sink;



    @Override
    public void start(ServiceBus<String> sinkTo) {
        this.sink = sinkTo;
        this.bulkSize = config.getBulkSize();
        String inputFile = config.getInputSourceFile();

        List<String> bulkBuffer = new ArrayList<>(bulkSize);
        try (Stream<String> stream = Files.lines(Paths.get(inputFile))) {
            AtomicInteger bulkIndex = new AtomicInteger();
            stream.forEach(x -> {
                bulkBuffer.add(x);
                if (bulkBuffer.size() == bulkSize) {
                    flush(bulkBuffer, bulkIndex.get());
                    bulkIndex.getAndIncrement();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flush(List<String> bulkBuffer,int bulkIndex)  {

        try {
            BulkMessage bulk = new BulkMessage(bulkBuffer,bulkIndex);
            String json  = mapper.writeValueAsString(bulk);
            sink.put(json);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        bulkBuffer.clear();
    }
}
