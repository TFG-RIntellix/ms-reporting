package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.AiReportGenerationException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.GenerateAiReportPort;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.gemini.GeminiReportPayload;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.gemini.GeminiRequest;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.gemini.GeminiRequest.GeminiContent;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.gemini.GeminiRequest.GeminiGenerationConfig;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.gemini.GeminiResponse;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.util.GeminiPromptFactory;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.config.HttpClientConfig;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that calls the Gemini API to produce the natural-language risk
 * report. The agent persona is set through the system instruction and the
 * output is forced into a strict JSON schema.
 */
@Slf4j
@Component
public class GeminiReportAdapter implements GenerateAiReportPort {

    private static final String GENERATE_CONTENT_PATH = "/v1beta/models/{model}:generateContent";
    private static final String RESPONSE_MIME_JSON = "application/json";
    private static final double TEMPERATURE = 0.2;
    private static final String USER_ROLE = "user";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiReportAdapter(
            @Qualifier(HttpClientConfig.GEMINI_CLIENT) final RestClient restClient,
            final ObjectMapper objectMapper,
            @Value("${gemini.api-key}") final String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") final String model) {
        this.restClient = Objects.requireNonNull(restClient);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.apiKey = Objects.requireNonNull(apiKey);
        this.model = Objects.requireNonNull(model);
    }

    @Override
    public AiReportContent generateReport(final ScoringData scoringData) {
        log.info(LogMessage.GEMINI_REQUEST_START, model, scoringData.getRequestId());

        final GeminiRequest request = buildRequest(scoringData);
        final GeminiResponse response = callGemini(request, scoringData.getRequestId());

        final String json = response != null ? response.firstText() : null;
        if (json == null || json.isBlank()) {
            throw new AiReportGenerationException(
                    String.format(LogMessage.GEMINI_RESPONSE_EMPTY, scoringData.getRequestId()));
        }

        return toDomain(parsePayload(json));
    }

    @Override
    public String getModelName() {
        return model;
    }

    private GeminiRequest buildRequest(final ScoringData scoringData) {
        final String scoringJson = serialize(scoringData);
        final GeminiContent systemInstruction =
                GeminiContent.ofText(null, GeminiPromptFactory.SYSTEM_INSTRUCTION);
        final GeminiContent userContent =
                GeminiContent.ofText(USER_ROLE, GeminiPromptFactory.buildUserMessage(scoringJson));
        final GeminiGenerationConfig generationConfig = new GeminiGenerationConfig(
                RESPONSE_MIME_JSON, GeminiPromptFactory.responseSchema(), TEMPERATURE);

        return new GeminiRequest(systemInstruction, List.of(userContent), generationConfig);
    }

    private GeminiResponse callGemini(final GeminiRequest request, final String requestId) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(GENERATE_CONTENT_PATH)
                            .queryParam("key", apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);
        } catch (RestClientException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_CALL_ERROR + " (requestId=" + requestId + ")", ex);
        }
    }

    private String serialize(final ScoringData scoringData) {
        try {
            return objectMapper.writeValueAsString(scoringData);
        } catch (JsonProcessingException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_CALL_ERROR, ex);
        }
    }

    private GeminiReportPayload parsePayload(final String json) {
        try {
            return objectMapper.readValue(json, GeminiReportPayload.class);
        } catch (JsonProcessingException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_PARSE_ERROR, ex);
        }
    }

    private AiReportContent toDomain(final GeminiReportPayload payload) {
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
