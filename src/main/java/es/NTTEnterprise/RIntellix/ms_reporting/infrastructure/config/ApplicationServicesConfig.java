package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.NTTEnterprise.RIntellix.ms_reporting.application.ports.input.ReportGenerationPortService;
import es.NTTEnterprise.RIntellix.ms_reporting.application.usecases.ReportGenerationService;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.FetchScoringPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.GenerateAiReportPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.RenderReportPdfPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.StoreReportPort;

/**
 * Wires the framework-agnostic application services to their output port
 * adapters, keeping the application layer free of Spring annotations.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Configuration
public class ApplicationServicesConfig {

    /**
     * Declares the ReportGenerationPortService bean coordinating report generation.
     *
     * @param fetchScoringPort     the port to fetch scoring data
     * @param generateAiReportPort the port to generate AI reports
     * @param renderReportPdfPort  the port to render PDF documents
     * @param storeReportPort      the port to persist reports
     * @return the report generation service instance
     */
    @Bean
    public ReportGenerationPortService reportGenerationPortService(
            final FetchScoringPort fetchScoringPort,
            final GenerateAiReportPort generateAiReportPort,
            final RenderReportPdfPort renderReportPdfPort,
            final StoreReportPort storeReportPort) {
        return new ReportGenerationService(
                fetchScoringPort, generateAiReportPort, renderReportPdfPort, storeReportPort);
    }
}
