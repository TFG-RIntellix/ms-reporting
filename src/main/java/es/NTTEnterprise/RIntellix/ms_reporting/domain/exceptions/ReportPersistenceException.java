package es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions;

/**
 * Raised when persisting the report through ms-core-data fails.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
public class ReportPersistenceException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ReportPersistenceException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ReportPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
