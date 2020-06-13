package com.bigId.FileAnalysisService;

import com.bigId.FileAnalysisService.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;


/***
 * FileAnalysisServiceApplication
 * Author : Tomer Shaiman
 */

@SpringBootApplication
public class FileAnalysisServiceApplication implements CommandLineRunner {

	@Autowired
	private AppConfig myConfig;

	private Orchestrator orchestrator; //in null on tests

	@Autowired
	private ApplicationContext context;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(FileAnalysisServiceApplication.class);
		app.run();
	}

	@Override
	public void run(String... args)  {
		System.out.println(myConfig.getEnvironment());
		try {
			Orchestrator orchestrator = context.getBean(Orchestrator.class);
			orchestrator.run();
		}catch (Exception ignored) {}
	}

}
