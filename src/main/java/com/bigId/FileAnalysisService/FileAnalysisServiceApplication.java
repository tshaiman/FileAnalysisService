package com.bigId.FileAnalysisService;

import com.bigId.FileAnalysisService.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/***
 * FileAnalysisServiceApplication
 * Author : Tomer Shaiman
 */

@SpringBootApplication
public class FileAnalysisServiceApplication implements CommandLineRunner {

	@Autowired
	private AppConfig myConfig;

	@Autowired
	private Orchestrator pipelineBootstrap;


	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FileAnalysisServiceApplication.class);
		app.run();
	}

	public void run(String... args)  {
		pipelineBootstrap.run();

	}
}
