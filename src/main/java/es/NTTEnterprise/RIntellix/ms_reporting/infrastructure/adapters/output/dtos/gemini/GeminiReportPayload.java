package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Structured JSON produced by the Gemini model (the report content), matching
 * the schema requested in the prompt.
 * 
 * @param title           the title of the generated report
 * @param aiSummary       the brief summary text of the report
 * @param riskAnalysis    the detailed risk analysis text
 * @param riskFactors     the list of identified risk factors
 * @param recommendations the list of recommendations
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiReportPayload(
        String title,
        @JsonProperty("ai_summary") String aiSummary,
        @JsonProperty("risk_analysis") String riskAnalysis,
        @JsonProperty("risk_factors") List<RiskFactorPayload> riskFactors,
        List<String> recommendations) {

    /**
     * Single risk factor payload properties.
     * 
     * @param factor      the name of the factor
     * @param severity    the severity tag
     * @param description the factor details
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RiskFactorPayload(String factor, String severity, String description) {
    }
}
