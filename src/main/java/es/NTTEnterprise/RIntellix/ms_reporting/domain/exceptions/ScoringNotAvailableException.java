package es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions;

/**
 * Raised when the scoring for a request is not yet available in ms-core-data.
 *
 * This is expected under the race condition where ms-reporting consumes the
 * persistScoring message before ms-core-data finished persisting the scoring.
 * It is retryable: the Kafka message will be redelivered until the scoring can
 * be retrieved.
 */
public class ScoringNotAvailableException extends RuntimeException {

    public ScoringNotAvailableException(final String message) {
        super(message);
    }

    public ScoringNotAvailableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
