package es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions;

/**
 * Raised when the PDF rendering of a report fails.
 */
public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(final String message) {
        super(message);
    }

    public PdfGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
