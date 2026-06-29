package es.NTTEnterprise.RIntellix.ms_reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the ms-reporting microservice.
 *
 * Consumes scoring results published by ms-risk-engine on the persistScoring
 * Kafka topic, builds an AI-generated risk report (via Gemini), renders it as a
 * PDF and persists the report document through ms-core-data.
 */
@SpringBootApplication
public class MsReportingApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MsReportingApplication.class, args);
    }
}
