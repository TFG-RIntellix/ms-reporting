package es.NTTEnterprise.RIntellix.ms_reporting.utils;

/**
 * Centralized constants for the ms-reporting microservice.
 * This class contains reusable and consistent constants to avoid hardcoding
 * values.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
public final class ReportConstants {

    private ReportConstants() {
        throw new UnsupportedOperationException(LogMessage.UTILITY_CLASS_NEVER_INSTANTIATE);
    }

    // General App Settings
    public static final String GENERATED_BY = "ms-reporting";
    public static final String LANGUAGE_SPANISH = "es";
    public static final String REQUEST_ID_REQUIRED_MSG = "requestId is required";
    public static final String REPORT_REQUIRED_MSG = "report is required";
    public static final String SCORING_RESPONSE_REQUIRED_MSG = "scoring response is required";
    public static final String NOT_AVAILABLE_STATUS = "N/A";

    // REST Paths for ms-core-data
    public static final String MS_CORE_DATA_SCORING_PATH = "/api/requests/{requestId}/scoring";
    public static final String MS_CORE_DATA_REQUEST_DETAILS_PATH = "/api/requests/{requestId}";
    public static final String MS_CORE_DATA_REPORTS_PATH = "/api/reports";

    // Gemini API Configurations
    public static final String GEMINI_GENERATE_CONTENT_PATH = "/v1beta/models/{model}:generateContent";
    public static final String RESPONSE_MIME_JSON = "application/json";
    public static final Float GEMINI_TEMPERATURE = 0.2F;
    public static final String GEMINI_USER_ROLE = "user";
    public static final String GEMINI_API_KEY_QUERY_PARAM = "key";

    // PDF Rendering Text
    public static final String PDF_DEFAULT_TITLE = "Informe de riesgo de crédito";
    public static final String PDF_SECTION_SUMMARY = "Resumen ejecutivo";
    public static final String PDF_SECTION_ANALYSIS = "Análisis de riesgo";
    public static final String PDF_SECTION_FACTORS = "Factores de riesgo";
    public static final String PDF_SECTION_RECOMMENDATIONS = "Recomendaciones";

    public static final String PDF_TABLE_HEADER_FACTOR = "Factor";
    public static final String PDF_TABLE_HEADER_SEVERITY = "Severidad";
    public static final String PDF_TABLE_HEADER_DESCRIPTION = "Descripción";

    public static final String PDF_HYPHEN = "-";
    public static final String PDF_BULLET_PREFIX = "• ";
    public static final String PDF_FILE_NAME_PATTERN = "report_%s_%s.pdf";

    public static final String PDF_META_REQUEST_PREFIX = "Request: ";
    public static final String PDF_META_SCORING_SEPARATOR = "  |  Scoring: ";
    public static final String PDF_META_MODEL_SEPARATOR = "  |  Modelo IA: ";
}
