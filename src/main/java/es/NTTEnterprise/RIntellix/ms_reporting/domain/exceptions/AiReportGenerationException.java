package es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions;

/**
 * Raised when the AI model fails to generate the report content.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
public class AiReportGenerationException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public AiReportGenerationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public AiReportGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
