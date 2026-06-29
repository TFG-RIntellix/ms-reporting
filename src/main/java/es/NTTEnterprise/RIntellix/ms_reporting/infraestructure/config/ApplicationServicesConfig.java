package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.config;

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
 */
@Configuration
public class ApplicationServicesConfig {

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
