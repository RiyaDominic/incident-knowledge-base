package com.incidentanalyzer;

import com.incidentanalyzer.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableAsync
@EnableMethodSecurity
@EnableMongoAuditing
@ConfigurationPropertiesScan(basePackageClasses = JwtProperties.class)
public class IncidentKnowledgeBaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentKnowledgeBaseApplication.class, args);
    }
}
