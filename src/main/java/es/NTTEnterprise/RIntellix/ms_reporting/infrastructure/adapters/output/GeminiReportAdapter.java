package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
 * @date 29/06/2026
 */
@Slf4j
@Component
public class GeminiReportAdapter implements GenerateAiReportPort {

    private final Client genaiClient;
    private final GeminiReportMapper geminiReportMapper;
    private final String model;

    /**
     * Constructs a GeminiReportAdapter with required Gen AI client, mapper and
     * config values.
     *
     * @param genaiClient        the Gen AI SDK client
     * @param geminiReportMapper the mapper for JSON conversion
     * @param model              the model version string
     */
    public GeminiReportAdapter(
            @Qualifier(HttpClientConfig.GEMINI_CLIENT) final Client genaiClient,
            final GeminiReportMapper geminiReportMapper,
            @Value("${gemini.model:gemini-3.1-flash-lite}") final String model) {
        this.genaiClient = Objects.requireNonNull(genaiClient);
        this.geminiReportMapper = Objects.requireNonNull(geminiReportMapper);
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

        return geminiReportMapper.fromJson(json);
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
            final String scoringJson = geminiReportMapper.toJson(scoringData);

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
}
