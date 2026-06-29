package es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions;

/**
 * Raised when persisting the report through ms-core-data fails.
 */
public class ReportPersistenceException extends RuntimeException {

    public ReportPersistenceException(final String message) {
        super(message);
    }

    public ReportPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
