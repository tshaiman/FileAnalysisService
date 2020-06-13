
package com.bigId.FileAnalysisService.pipeline;

import com.bigId.FileAnalysisService.config.AppConfig;
import com.bigId.FileAnalysisService.contracts.Position;
import com.bigId.FileAnalysisService.servicebus.BulkMessage;
import com.bigId.FileAnalysisService.servicebus.MatcherResult;
import com.bigId.FileAnalysisService.servicebus.ServiceBus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class FileAnalysisIntegrationTests {


    @Autowired
    private AppConfig appConfig;

    @Autowired
    private MatcherService matcherService;

    @Autowired
    private ApplicationContext context;

    @Bean
    private MatcherService matcher() {
        return new MatcherService();
    }

    @Test
    public void whenJohnExists_thenJohnShouldBeFound() {
        //from original text line 350
        String line = "\"The Church of St. Monica, John,' she cried, 'and half a sovereign if you reach it in twenty minutes.\"";
        List<MatcherResult> mr =this.matcherService.processLine(line,350);
        assertThat(mr.size()).isEqualTo(1);
        MatcherResult singleMatchResult = mr.get(0);
        assertThat(singleMatchResult.getName()).isEqualTo("John");
        Position p = new Position(350,27);
        assertThat(singleMatchResult.getPositions().get(0)).isEqualTo(p);
    }

    @Test
    public void whenJohnsExists_thenJohnShoulNotdBeFound() {
        String line = "\"Is Briony Lodge, Serpentine Avenue, St. john's Wood.\""; // from original text line 309
        List<MatcherResult> mr = this.matcherService.processLine(line,309);
        assertThat(mr.size()).isEqualTo(0);
    }

    @Test
    public void matchLine_bulkTest() {
        String line = "\"Is Briony Lodge, Serpentine Avenue, St. John's Wood.\""; // from original text line 309
        String line2 = "\" 'The Church of St. Monica, Paul, she cried, 'and half a sovereign if you reach it in twenty minutes.\"";
        BulkMessage bm = new BulkMessage(Arrays.asList(line,line2),5);
        ServiceBus<String> sink = new ServiceBus<>();
        this.matcherService.setSink(sink);
        this.matcherService.process(bm);


        try {
            String m1 = sink.poll(4000);
            ObjectMapper objectMapper = new ObjectMapper();
            MatcherResult result1 = objectMapper.readValue(m1,MatcherResult.class);
            assertThat(result1.getName()).isEqualTo("John");
            Position p = new Position(5001,41);
            assertThat(result1.getPositions().get(0)).isEqualTo(p);
            //second message in the bulk
            m1 = sink.poll(4000);
            MatcherResult result2 = objectMapper.readValue(m1,MatcherResult.class);
            assertThat(result2.getName()).isEqualTo("Paul");
            p = new Position(5002,29);
            assertThat(result2.getPositions().get(0)).isEqualTo(p);


        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }



        //TODO : Write more tests here.............

    }
}
