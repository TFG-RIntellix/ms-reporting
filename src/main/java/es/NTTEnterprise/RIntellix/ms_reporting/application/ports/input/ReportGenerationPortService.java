package es.NTTEnterprise.RIntellix.ms_reporting.application.ports.input;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;

/**
 * Input port for the report generation use case.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
public interface ReportGenerationPortService {

    /**
     * Generates and persists a risk report for the given request.
     *
     * @param requestId the request identifier from the persistScoring message
     * @return the generated report
     */
    Report generateReport(String requestId);
}
