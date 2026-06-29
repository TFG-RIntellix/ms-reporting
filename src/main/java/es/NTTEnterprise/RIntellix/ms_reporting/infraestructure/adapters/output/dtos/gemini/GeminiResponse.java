package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.gemini;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Response body for the Gemini {@code generateContent} endpoint (only the
 * fields ms-reporting needs).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(List<Candidate> candidates) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(Content content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(List<Part> parts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Part(String text) {
    }

    /**
     * @return the text of the first candidate's first part, or {@code null}.
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
