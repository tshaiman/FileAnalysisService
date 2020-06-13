package com.bigId.FileAnalysisService;

import com.bigId.FileAnalysisService.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


/***
 * FileAnalysisServiceApplication
 * Author : Tomer Shaiman
 */

@SpringBootApplication
public class FileAnalysisServiceApplication implements CommandLineRunner {

    @Autowired
    private AppConfig myConfig;

    private Orchestrator orchestrator;

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FileAnalysisServiceApplication.class);
        app.run();
    }

    @Override
    public void run(String... args) {
        try {
            context.getBean(Orchestrator.class);
        } catch (Exception ignored) {
        }
    }

}
