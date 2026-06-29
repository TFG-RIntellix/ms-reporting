package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

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
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.AiReportGenerationException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.GenerateAiReportPort;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini.GeminiReportPayload;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini.GeminiRequest;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini.GeminiRequest.GeminiContent;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini.GeminiRequest.GeminiGenerationConfig;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini.GeminiResponse;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.util.GeminiPromptFactory;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.config.HttpClientConfig;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers.GeminiReportMapper;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that calls the Gemini API to produce the natural-language risk
 * report. The agent persona is set through the system instruction and the
 * output is forced into a strict JSON schema.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Slf4j
@Component
public class GeminiReportAdapter implements GenerateAiReportPort {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    /**
     * Constructs a GeminiReportAdapter with required rest client, mapper and config values.
     *
     * @param restClient   the rest client qualified for Gemini API
     * @param objectMapper the object mapper for serialization
     * @param apiKey       the configured Gemini API key
     * @param model        the model version string
     */
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

        return GeminiReportMapper.toDomain(parsePayload(json));
    }

    @Override
    public String getModelName() {
        return model;
    }

    /**
     * Builds the request body payload for Gemini API.
     *
     * @param scoringData the scoring details mapping into user instructions
     * @return the constructed GeminiRequest
     */
    private GeminiRequest buildRequest(final ScoringData scoringData) {
        final String scoringJson = serialize(scoringData);
        final GeminiContent systemInstruction =
                GeminiContent.ofText(null, GeminiPromptFactory.SYSTEM_INSTRUCTION);
        final GeminiContent userContent =
                GeminiContent.ofText(ReportConstants.GEMINI_USER_ROLE, GeminiPromptFactory.buildUserMessage(scoringJson));
        final GeminiGenerationConfig generationConfig = new GeminiGenerationConfig(
                ReportConstants.RESPONSE_MIME_JSON, GeminiPromptFactory.responseSchema(), ReportConstants.GEMINI_TEMPERATURE);

        return new GeminiRequest(systemInstruction, List.of(userContent), generationConfig);
    }

    /**
     * Executes the REST POST query calling Gemini.
     *
     * @param request   the GeminiRequest body
     * @param requestId the request ID for error details mapping
     * @return the received GeminiResponse
     */
    private GeminiResponse callGemini(final GeminiRequest request, final String requestId) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(ReportConstants.GEMINI_GENERATE_CONTENT_PATH)
                            .queryParam(ReportConstants.GEMINI_API_KEY_QUERY_PARAM, apiKey)
                            .build(model))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);
        } catch (RestClientException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_CALL_ERROR + " (requestId=" + requestId + ")", ex);
        }
    }

    /**
     * Serializes scoring data into a JSON string.
     *
     * @param scoringData the scoring entity
     * @return the serialized JSON string
     */
    private String serialize(final ScoringData scoringData) {
        try {
            return objectMapper.writeValueAsString(scoringData);
        } catch (JsonProcessingException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_CALL_ERROR, ex);
        }
    }

    /**
     * Parses the response JSON string from Gemini back into a structured payload.
     *
     * @param json the raw response string
     * @return the parsed payload
     */
    private GeminiReportPayload parsePayload(final String json) {
        try {
            return objectMapper.readValue(json, GeminiReportPayload.class);
        } catch (JsonProcessingException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_PARSE_ERROR, ex);
        }
    }
}
