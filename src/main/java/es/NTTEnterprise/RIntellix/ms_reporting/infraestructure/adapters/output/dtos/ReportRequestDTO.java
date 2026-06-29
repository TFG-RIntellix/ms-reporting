package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

/**
 * Body sent to ms-core-data's {@code POST /api/reports} endpoint. Field names
 * follow the reports collection schema (snake_case).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportRequestDTO {

    @JsonProperty("party_id")
    private final String partyId;

    @JsonProperty("request_id")
    private final String requestId;

    @JsonProperty("scoring_id")
    private final String scoringId;

    @JsonProperty("report_type")
    private final String reportType;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("ai_summary")
    private final String aiSummary;

    @JsonProperty("risk_analysis")
    private final String riskAnalysis;

    @JsonProperty("risk_factors")
    private final List<RiskFactorDTO> riskFactors;

    @JsonProperty("recommendations")
    private final List<String> recommendations;

    @JsonProperty("file_path")
    private final String filePath;

    @JsonProperty("file_size_bytes")
    private final int fileSizeBytes;

    @JsonProperty("generated_by")
    private final String generatedBy;

    @JsonProperty("generated_date")
    private final Date generatedDate;

    @JsonProperty("generation_time_ms")
    private final int generationTimeMs;

    @JsonProperty("model_version")
    private final String modelVersion;

    @JsonProperty("language")
    private final String language;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskFactorDTO {

        @JsonProperty("factor")
        private final String factor;

        @JsonProperty("severity")
        private final String severity;

        @JsonProperty("description")
        private final String description;
    }
}
