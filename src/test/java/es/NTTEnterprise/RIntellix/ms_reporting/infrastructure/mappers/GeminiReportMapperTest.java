package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.AiReportGenerationException;

class GeminiReportMapperTest {

    private GeminiReportMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mapper = new GeminiReportMapper(objectMapper);
    }

    @Test
    void shouldSerializeScoringDataToJson() {
        ScoringData data = new ScoringData();
        data.setRequestId("req-123");
        data.setPd(0.05);

        String json = mapper.toJson(data);

        assertNotNull(json);
        assertNotNull(json.contains("req-123"));
    }

    @Test
    void shouldParseJsonToAiReportContent() {
        String json = """
                {
                  "title": "Informe IA",
                  "ai_summary": "Resumen",
                  "risk_analysis": "Analisis",
                  "risk_factors": [
                    {
                      "factor": "Factor 1",
                      "severity": "ALTO",
                      "description": "Desc 1"
                    }
                  ],
                  "recommendations": ["Rec 1"]
                }
                """;

        AiReportContent content = mapper.fromJson(json);

        assertNotNull(content);
        assertEquals("Informe IA", content.getTitle());
        assertEquals("Resumen", content.getAiSummary());
        assertEquals(1, content.getRiskFactors().size());
        assertEquals(Severity.ALTO, content.getRiskFactors().get(0).getSeverity());
    }

    @Test
    void shouldThrowExceptionOnInvalidJson() {
        String invalidJson = "{ invalid }";

        assertThrows(AiReportGenerationException.class, () -> {
            mapper.fromJson(invalidJson);
        });
    }
}
