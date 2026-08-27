package es.NTTEnterprise.RIntellix.ms_reporting.domain.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.ReportType;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;

class ReportFactoryTest {

    @Test
    void shouldCreateReportSuccessfully() {
        ScoringData data = new ScoringData();
        data.setPartyId("party-123");
        data.setRequestId("req-123");
        data.setScoringId("score-123");
        data.setPartyName("Cliente de Prueba");

        AiReportContent aiContent = AiReportContent.builder()
                .title("Título IA")
                .aiSummary("Resumen de riesgo")
                .riskAnalysis("Análisis profundo")
                .recommendations(List.of("Rec 1"))
                .build();

        Report report = ReportFactory.createReport(data, aiContent, "gemini-model");

        assertNotNull(report);
        assertEquals("party-123", report.getPartyId());
        assertEquals("req-123", report.getRequestId());
        assertEquals("score-123", report.getScoringId());
        assertEquals(ReportType.RISK_ANALYSIS, report.getReportType());
        assertEquals(ReportConstants.REPORT_TITLE_PREFIX + "Cliente de Prueba", report.getTitle());
        assertEquals("Resumen de riesgo", report.getAiSummary());
        assertEquals("gemini-model", report.getModelVersion());
        assertNotNull(report.getGeneratedDate());
    }

    @Test
    void shouldFallbackToAiTitleIfPartyNameIsMissing() {
        ScoringData data = new ScoringData();
        
        AiReportContent aiContent = AiReportContent.builder()
                .title("Título Generado por IA")
                .build();

        Report report = ReportFactory.createReport(data, aiContent, "gemini-model");

        assertEquals("Título Generado por IA", report.getTitle());
    }
}
