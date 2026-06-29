package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request body for the Gemini {@code generateContent} endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(
        GeminiContent systemInstruction,
        List<GeminiContent> contents,
        GeminiGenerationConfig generationConfig) {

    public record GeminiContent(String role, List<GeminiPart> parts) {

        public static GeminiContent ofText(final String role, final String text) {
            return new GeminiContent(role, List.of(new GeminiPart(text)));
        }
    }

    public record GeminiPart(String text) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiGenerationConfig(
            String responseMimeType,
            Object responseSchema,
            Double temperature) {
    }
}
