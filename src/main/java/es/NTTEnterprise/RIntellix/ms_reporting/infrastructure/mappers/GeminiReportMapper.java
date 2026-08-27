package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonProperty;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.AiReportGenerationException;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;

/**
 * Maps the Gemini JSON response payload to the domain {@link AiReportContent}
 * aggregate
 * and serializes {@link ScoringData} into JSON requests.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Component
public class GeminiReportMapper {

    private final ObjectMapper objectMapper;

    /**
     * Constructs a GeminiReportMapper with the required ObjectMapper.
     *
     * @param objectMapper the Jackson object mapper for JSON processing.
     */
    public GeminiReportMapper(final ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    /**
     * Data Transfer Object for parsing the Gemini JSON response.
     */
    public record GeminiReportPayload(
            String title,
            @JsonProperty("ai_summary") String aiSummary,
            @JsonProperty("risk_analysis") String riskAnalysis,
            @JsonProperty("risk_factors") List<RiskFactorPayload> riskFactors,
            List<String> recommendations) {
    }

    /**
     * Nested Data Transfer Object for risk factors within the Gemini payload.
     */
    public record RiskFactorPayload(
            String factor,
            String severity,
            String description) {
    }

    /**
     * Serializes scoring data into a JSON string.
     *
     * @param scoringData the scoring entity to serialize.
     * @return the serialized JSON string.
     * @throws AiReportGenerationException if serialization fails.
     */
    public String toJson(final ScoringData scoringData) {
        try {
            return objectMapper.writeValueAsString(scoringData);
        } catch (JsonProcessingException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_CALL_ERROR, ex);
        }
    }

    /**
     * Parses the response JSON string from Gemini back into an AiReportContent
     * domain entity.
     *
     * @param json the raw response string from Gemini.
     * @return the constructed AiReportContent domain entity.
     * @throws AiReportGenerationException if parsing fails.
     */
    public AiReportContent fromJson(final String json) {
        try {
            GeminiReportPayload payload = objectMapper.readValue(json, GeminiReportPayload.class);
            return toDomain(payload);
        } catch (JsonProcessingException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_PARSE_ERROR, ex);
        }
    }

    /**
     * Translates the GeminiReportPayload DTO into AiReportContent domain entity.
     *
     * @param payload the parsed JSON report details.
     * @return the constructed AiReportContent domain entity.
     */
    private AiReportContent toDomain(final GeminiReportPayload payload) {
        if (payload == null) {
            return null;
        }

        final List<RiskFactor> riskFactors = payload.riskFactors() == null ? List.of()
                : payload.riskFactors().stream()
                        .map(rf -> RiskFactor.builder()
                                .factor(rf.factor())
                                .severity(toSeverity(rf.severity()))
                                .description(rf.description())
                                .build())
                        .toList();

        return AiReportContent.builder()
                .title(payload.title())
                .aiSummary(payload.aiSummary())
                .riskAnalysis(payload.riskAnalysis())
                .riskFactors(riskFactors)
                .recommendations(payload.recommendations() == null ? List.of() : payload.recommendations())
                .build();
    }

    /**
     * Normalizes the severity string into a Severity enum value.
     *
     * @param value raw severity string.
     * @return normalized Severity enum value, defaults to MEDIO.
     */
    private Severity toSeverity(final String value) {
        if (value == null) {
            return Severity.MEDIO;
        }
        try {
            return Severity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Severity.MEDIO;
        }
    }
}
