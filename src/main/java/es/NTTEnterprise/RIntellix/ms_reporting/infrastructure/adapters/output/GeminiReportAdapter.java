package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.AiReportGenerationException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.GenerateAiReportPort;
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

    private final Client genaiClient;
    private final ObjectMapper objectMapper;
    private final String model;

    /**
     * Constructs a GeminiReportAdapter with required Gen AI client, mapper and
     * config values.
     *
     * @param genaiClient  the Gen AI SDK client
     * @param objectMapper the object mapper for serialization
     * @param model        the model version string
     */
    public GeminiReportAdapter(
            @Qualifier(HttpClientConfig.GEMINI_CLIENT) final Client genaiClient,
            final ObjectMapper objectMapper,
            @Value("${gemini.model:gemini-3.5-flash}") final String model) {
        this.genaiClient = Objects.requireNonNull(genaiClient);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.model = Objects.requireNonNull(model);
    }

    @Override
    public AiReportContent generateReport(final ScoringData scoringData) {
        log.info(LogMessage.GEMINI_REQUEST_START, model, scoringData.getRequestId());

        final String json = callGemini(scoringData);

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
     * Executes the call to Gemini using the Gen AI SDK.
     *
     * @param scoringData the scoring entity
     * @return the generated response text
     */
    private String callGemini(final ScoringData scoringData) {
        try {
            final String scoringJson = serialize(scoringData);

            final GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(Content.builder()
                            .parts(List.of(Part.builder().text(GeminiPromptFactory.SYSTEM_INSTRUCTION).build()))
                            .build())
                    .responseMimeType(ReportConstants.RESPONSE_MIME_JSON)
                    .responseSchema(GeminiPromptFactory.responseSchema())
                    .temperature(ReportConstants.GEMINI_TEMPERATURE)
                    .build();

            final Content userContent = Content.builder()
                    .role(ReportConstants.GEMINI_USER_ROLE)
                    .parts(List.of(Part.builder().text(GeminiPromptFactory.buildUserMessage(scoringJson)).build()))
                    .build();

            final GenerateContentResponse response = genaiClient.models.generateContent(model,
                    userContent, config);

            return response.text();
        } catch (Exception ex) {
            throw new AiReportGenerationException(
                    LogMessage.GEMINI_CALL_ERROR + " (requestId=" + scoringData.getRequestId() + ")", ex);
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
    private GeminiReportMapper.GeminiReportPayload parsePayload(final String json) {
        try {
            return objectMapper.readValue(json, GeminiReportMapper.GeminiReportPayload.class);
        } catch (JsonProcessingException ex) {
            throw new AiReportGenerationException(LogMessage.GEMINI_PARSE_ERROR, ex);
        }
    }
}
