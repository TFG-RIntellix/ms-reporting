package es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions;

/**
 * Raised when the PDF rendering of a report fails.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
public class PdfGenerationException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public PdfGenerationException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public PdfGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
