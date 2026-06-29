package es.NTTEnterprise.RIntellix.ms_reporting.es.NTTEnterprise.app.ms_reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Entry point for the ms-reporting microservice.
 * Consumes scoring results published by ms-risk-engine on the persistScoring
 * Kafka topic, builds an AI-generated risk report (via Gemini), renders it as a
 * PDF and persists the report document through ms-core-data.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@SpringBootApplication
@EnableRetry
public class MsReportingApplication {

    /**
     * Main method running the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(MsReportingApplication.class, args);
    }
}
