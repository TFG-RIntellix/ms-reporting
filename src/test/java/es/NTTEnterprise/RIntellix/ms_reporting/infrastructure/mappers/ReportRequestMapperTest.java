package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.ReportType;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.ReportRequestDTO;

class ReportRequestMapperTest {

    @Test
    @DisplayName("Should map Report to ReportRequestDTO successfully")
    void toRequest_success() {
        Report report = Report.builder()
                .partyId("P-1")
                .requestId("REQ-1")
                .scoringId("SCO-1")
                .reportType(ReportType.RISK_ANALYSIS)
                .title("Report Title")
                .aiSummary("Summary")
                .riskAnalysis("Analysis")
                .riskFactors(List.of(
                    RiskFactor.builder().factor("F1").severity(Severity.ALTO).description("D1").build()
                ))
                .recommendations(List.of("Rec1"))
                .filePath("/tmp/report.pdf")
                .fileSizeBytes(1024)
                .generatedBy("System")
                .generatedDate(new Date())
                .generationTimeMs(500)
                .modelVersion("gemini-1.5")
                .language("es")
                .build();

        ReportRequestDTO dto = ReportRequestMapper.toRequest(report);

        assertEquals("P-1", dto.getPartyId());
        assertEquals("REQ-1", dto.getRequestId());
        assertEquals("SCO-1", dto.getScoringId());
        assertEquals("RISK_ANALYSIS", dto.getReportType());
        assertEquals("Report Title", dto.getTitle());
        assertEquals("Summary", dto.getAiSummary());
        assertEquals("Analysis", dto.getRiskAnalysis());
        assertEquals(1, dto.getRiskFactors().size());
        assertEquals("F1", dto.getRiskFactors().get(0).getFactor());
        assertEquals("ALTO", dto.getRiskFactors().get(0).getSeverity());
        assertEquals("D1", dto.getRiskFactors().get(0).getDescription());
        assertEquals(1, dto.getRecommendations().size());
        assertEquals("/tmp/report.pdf", dto.getFilePath());
        assertEquals(1024, dto.getFileSizeBytes());
        assertEquals("System", dto.getGeneratedBy());
        assertEquals(500, dto.getGenerationTimeMs());
        assertEquals("gemini-1.5", dto.getModelVersion());
        assertEquals("es", dto.getLanguage());
    }

    @Test
    @DisplayName("Should map Report with nulls")
    void toRequest_nulls() {
        Report report = Report.builder().build();
        ReportRequestDTO dto = ReportRequestMapper.toRequest(report);
        
        assertNull(dto.getPartyId());
        assertNull(dto.getReportType());
        assertNotNull(dto.getRiskFactors());
        assertTrue(dto.getRiskFactors().isEmpty());
    }
}
