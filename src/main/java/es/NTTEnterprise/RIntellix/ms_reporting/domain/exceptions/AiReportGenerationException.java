package es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions;

/**
 * Raised when the AI model fails to generate the report content.
 */
public class AiReportGenerationException extends RuntimeException {

    public AiReportGenerationException(final String message) {
        super(message);
    }

    public AiReportGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
