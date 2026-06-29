package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request body for the Gemini {@code generateContent} endpoint.
 * 
 * @param systemInstruction the system instruction content
 * @param contents          the list of request contents (messages)
 * @param generationConfig  the parameters config
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(
        GeminiContent systemInstruction,
        List<GeminiContent> contents,
        GeminiGenerationConfig generationConfig) {

    /**
     * Message content.
     * 
     * @param role  the role of the sender
     * @param parts the list of content parts
     */
    public record GeminiContent(String role, List<GeminiPart> parts) {

        /**
         * Helper static constructor for text parts.
         *
         * @param role the sender role
         * @param text the message text
         * @return the GeminiContent instance
         */
        public static GeminiContent ofText(final String role, final String text) {
            return new GeminiContent(role, List.of(new GeminiPart(text)));
        }
    }

    /**
     * Text segment wrapper.
     * 
     * @param text the raw string content
     */
    public record GeminiPart(String text) {
    }

    /**
     * Configuration parameters mapping.
     * 
     * @param responseMimeType the expected response mime type
     * @param responseSchema   the validation schema object
     * @param temperature      the model temperature parameter
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiGenerationConfig(
            String responseMimeType,
            Object responseSchema,
            Double temperature) {
    }
}
