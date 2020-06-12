package com.bigId.FileAnalysisService.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Data
public class AppConfig {

    private String environment;

    private String inputSourceFile;

    @Value("#{'${lookups}'.split(',')}")
    private List<String> lookups = new ArrayList<>();

    private int bulkSize;

    private int degreeOfParallelism;

}