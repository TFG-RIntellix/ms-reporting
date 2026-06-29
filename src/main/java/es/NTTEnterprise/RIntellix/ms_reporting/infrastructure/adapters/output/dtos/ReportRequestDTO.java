package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

/**
 * Body sent to ms-core-data's {@code POST /api/reports} endpoint. Field names
 * follow the reports collection schema (snake_case).
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportRequestDTO {

    /**
     * Unique identifier of the associated party.
     */
    @JsonProperty("party_id")
    private final String partyId;

    /**
     * Unique identifier of the credit request.
     */
    @JsonProperty("request_id")
    private final String requestId;

    /**
     * Unique identifier of the computed scoring.
     */
    @JsonProperty("scoring_id")
    private final String scoringId;

    /**
     * Type of report generated.
     */
    @JsonProperty("report_type")
    private final String reportType;

    /**
     * The title of the generated report.
     */
    @JsonProperty("title")
    private final String title;

    /**
     * A brief AI generated summary.
     */
    @JsonProperty("ai_summary")
    private final String aiSummary;

    /**
     * Detailed credit risk analysis.
     */
    @JsonProperty("risk_analysis")
    private final String riskAnalysis;

    /**
     * List of identified risk factors.
     */
    @JsonProperty("risk_factors")
    private final List<RiskFactorDTO> riskFactors;

    /**
     * List of actionable recommendations.
     */
    @JsonProperty("recommendations")
    private final List<String> recommendations;

    /**
     * The path to the stored PDF file on disk.
     */
    @JsonProperty("file_path")
    private final String filePath;

    /**
     * Size of the generated PDF report in bytes.
     */
    @JsonProperty("file_size_bytes")
    private final int fileSizeBytes;

    /**
     * Service or agent that generated this report.
     */
    @JsonProperty("generated_by")
    private final String generatedBy;

    /**
     * Timestamp when the report was generated.
     */
    @JsonProperty("generated_date")
    private final Date generatedDate;

    /**
     * Time taken to generate the report in milliseconds.
     */
    @JsonProperty("generation_time_ms")
    private final int generationTimeMs;

    /**
     * Version of the AI model used to generate the report content.
     */
    @JsonProperty("model_version")
    private final String modelVersion;

    /**
     * Language of the generated report.
     */
    @JsonProperty("language")
    private final String language;

    /**
     * Risk Factor item details sent inside JSON body.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskFactorDTO {

        /**
         * The name or title of the risk factor.
         */
        @JsonProperty("factor")
        private final String factor;

        /**
         * The severity level of this risk factor.
         */
        @JsonProperty("severity")
        private final String severity;

        /**
         * Detailed explanation of the risk factor.
         */
        @JsonProperty("description")
        private final String description;
    }
}
