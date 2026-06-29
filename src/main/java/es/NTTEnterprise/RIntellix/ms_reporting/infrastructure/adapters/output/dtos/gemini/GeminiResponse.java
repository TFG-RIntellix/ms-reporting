package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response body for the Gemini {@code generateContent} endpoint (only the
 * fields ms-reporting needs).
 * 
 * @param candidates the list of content response candidates
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(List<Candidate> candidates) {

    /**
     * Response candidate wrapper.
     * 
     * @param content the candidate message content
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content) {
    }

    /**
     * Message content.
     * 
     * @param parts the list of content parts
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(List<Part> parts) {
    }

    /**
     * Content part.
     * 
     * @param text the text payload
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(String text) {
    }

    /**
     * Resolves the first content text block.
     *
     * @return the text of the first candidate's first part, or {@code null}
     */
    public String firstText() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        final Candidate candidate = candidates.get(0);
        if (candidate == null || candidate.content() == null
                || candidate.content().parts() == null || candidate.content().parts().isEmpty()) {
            return null;
        }
        return candidate.content().parts().get(0).text();
    }
}
