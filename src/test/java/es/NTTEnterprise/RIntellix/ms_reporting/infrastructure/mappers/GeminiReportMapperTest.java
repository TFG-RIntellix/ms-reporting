package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;

class GeminiReportMapperTest {

    @Test
    @DisplayName("Should map payload successfully")
    void toDomain_success() {
        var riskFactor = new GeminiReportMapper.RiskFactorPayload("F1", "ALTO", "Desc1");
        var payload = new GeminiReportMapper.GeminiReportPayload(
            "Title",
            "Summary",
            "Analysis",
            List.of(riskFactor),
            List.of("Rec1")
        );

        AiReportContent content = GeminiReportMapper.toDomain(payload);

        assertEquals("Title", content.getTitle());
        assertEquals("Summary", content.getAiSummary());
        assertEquals("Analysis", content.getRiskAnalysis());
        assertEquals(1, content.getRiskFactors().size());
        assertEquals("F1", content.getRiskFactors().get(0).getFactor());
        assertEquals(Severity.ALTO, content.getRiskFactors().get(0).getSeverity());
        assertEquals("Desc1", content.getRiskFactors().get(0).getDescription());
        assertEquals(1, content.getRecommendations().size());
        assertEquals("Rec1", content.getRecommendations().get(0));
    }

    @Test
    @DisplayName("Should return null if payload is null")
    void toDomain_null() {
        assertNull(GeminiReportMapper.toDomain(null));
    }

    @Test
    @DisplayName("Should map unknown severity to MEDIO")
    void toDomain_unknownSeverity() {
        var riskFactor = new GeminiReportMapper.RiskFactorPayload("F1", "UNKNOWN", "Desc1");
        var payload = new GeminiReportMapper.GeminiReportPayload(
            "Title", "Summary", "Analysis", List.of(riskFactor), null
        );

        AiReportContent content = GeminiReportMapper.toDomain(payload);
        assertEquals(Severity.MEDIO, content.getRiskFactors().get(0).getSeverity());
        assertTrue(content.getRecommendations().isEmpty());
    }

    @Test
    @DisplayName("Should map null severity to MEDIO")
    void toDomain_nullSeverity() {
        var riskFactor = new GeminiReportMapper.RiskFactorPayload("F1", null, "Desc1");
        var payload = new GeminiReportMapper.GeminiReportPayload(
            "Title", "Summary", "Analysis", List.of(riskFactor), null
        );

        AiReportContent content = GeminiReportMapper.toDomain(payload);
        assertEquals(Severity.MEDIO, content.getRiskFactors().get(0).getSeverity());
    }
}
