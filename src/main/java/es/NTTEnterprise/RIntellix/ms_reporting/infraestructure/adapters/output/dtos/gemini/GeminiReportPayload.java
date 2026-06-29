package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Structured JSON produced by the Gemini model (the report content), matching
 * the schema requested in the prompt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiReportPayload(
        String title,
        @JsonProperty("ai_summary") String aiSummary,
        @JsonProperty("risk_analysis") String riskAnalysis,
        @JsonProperty("risk_factors") List<RiskFactorPayload> riskFactors,
        List<String> recommendations) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RiskFactorPayload(String factor, String severity, String description) {
    }
}
