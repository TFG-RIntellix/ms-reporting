package es.NTTEnterprise.RIntellix.ms_reporting.utils;

/**
 * Centralized log message templates for the ms-reporting microservice.
 * This class provides consistent and reusable log message templates
 * to ensure uniform logging across all layers of the application.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
public final class LogMessage {

    public static final String UTILITY_CLASS_NEVER_INSTANTIATE = "Never instantiate";

    private LogMessage() {
        throw new UnsupportedOperationException(UTILITY_CLASS_NEVER_INSTANTIATE);
    }

    public static final String LOG_VALUE_UNKNOWN = "UNKNOWN";

    // Kafka consumer
    public static final String KAFKA_MESSAGE_RECEIVED =
            "persistScoring message received [key={}, topic={}, offset={}]";
    public static final String KAFKA_MESSAGE_NULL_REQUEST_ID =
            "Received persistScoring message without requestId";
    public static final String KAFKA_MESSAGE_PROCESSED =
            "Report generated and message acknowledged for requestId={}";
    public static final String KAFKA_CONSUMER_ERROR = 
            "Error processing persistScoring message at offset {}: {}";

    // Report generation service
    public static final String REPORT_GENERATION_START =
            "Starting report generation for requestId={}";
    public static final String REPORT_SCORING_FETCHED =
            "Scoring fetched for requestId={} [scoringId={}, partyId={}]";
    public static final String REPORT_AI_GENERATED =
            "AI report generated for requestId={} [model={}, riskFactors={}]";
    public static final String REPORT_PDF_RENDERED =
            "PDF rendered for requestId={} [sizeBytes={}, filePath={}]";
    public static final String REPORT_PERSISTED =
            "Report persisted for requestId={} [scoringId={}] in {} ms";

    // ms-core-data scoring adapter
    public static final String SCORING_FETCH_START =
            "Fetching scoring from ms-core-data for requestId={}";
    public static final String SCORING_NOT_AVAILABLE =
            "Scoring not yet available in ms-core-data for requestId=%s (status=%s)";
    public static final String SCORING_FETCH_ERROR =
            "Error fetching scoring from ms-core-data for requestId=%s (status=%s)";
    public static final String PARTY_RESOLVE_WARN =
            "Could not resolve partyId for requestId={}; report will be stored without it";

    // Gemini adapter
    public static final String GEMINI_REQUEST_START =
            "Requesting AI risk report from Gemini [model={}] for requestId={}";
    public static final String GEMINI_RESPONSE_EMPTY =
            "Gemini returned an empty response for requestId=%s";
    public static final String GEMINI_PARSE_ERROR =
            "Unable to parse Gemini response into report content";
    public static final String GEMINI_CALL_ERROR =
            "Gemini API call failed";

    // PDF adapter
    public static final String PDF_RENDER_ERROR =
            "Failed to render report PDF";
    public static final String PDF_WRITE_WARN =
            "Failed to write report PDF to {}; storing report without file_path";

    // Report store adapter
    public static final String REPORT_POST_START =
            "Persisting report through ms-core-data for requestId={}";
    public static final String REPORT_POST_ERROR =
            "Failed to persist report through ms-core-data for requestId=%s (status=%s)";
}
