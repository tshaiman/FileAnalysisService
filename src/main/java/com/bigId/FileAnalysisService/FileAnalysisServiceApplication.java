package com.bigId.FileAnalysisService;

import com.bigId.FileAnalysisService.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



@SpringBootApplication
public class FileAnalysisServiceApplication implements CommandLineRunner {

	@Autowired
	private AppConfig myConfig;

	@Autowired
	private Bootstrap pipelineBootstrap;



	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FileAnalysisServiceApplication.class);
		app.run();
	}

	public void run(String... args) throws Exception {
		pipelineBootstrap.build();
	}
}
