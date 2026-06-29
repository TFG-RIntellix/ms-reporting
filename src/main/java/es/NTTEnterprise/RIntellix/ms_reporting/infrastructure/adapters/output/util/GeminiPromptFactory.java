package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;

/**
 * Builds the prompt artefacts for the Gemini analyst agent: the system
 * instruction (persona), the user message and the JSON response schema used to
 * force structured output.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
public final class GeminiPromptFactory {

    private GeminiPromptFactory() {
        throw new UnsupportedOperationException(LogMessage.UTILITY_CLASS_NEVER_INSTANTIATE);
    }

    /**
     * System instruction defining the agent behaviour: a financial risk analyst
     * that always answers in Spanish with a strict JSON object.
     */
    public static final String SYSTEM_INSTRUCTION = """
            Actúa como un analista experto en riesgo financiero y riesgo de crédito de una entidad bancaria.
            Tu tarea es generar un informe detallado, riguroso y profesional sobre el riesgo de una solicitud
            de crédito, a partir del objeto de scoring proporcionado (probabilidad de impago, severidad de la
            pérdida, exposición, pérdida esperada, grado de riesgo, métricas financieras y factores
            explicativos SHAP).

            Requisitos:
            - Redacta SIEMPRE en español, con un tono técnico y claro.
            - Basa tus conclusiones únicamente en los datos proporcionados; no inventes cifras.
            - Identifica los factores de riesgo más relevantes y clasifícalos por severidad (BAJO, MEDIO, ALTO).
            - Proporciona recomendaciones accionables para el analista de riesgos.
            - Devuelve EXCLUSIVAMENTE un objeto JSON válido que cumpla el esquema indicado, sin texto adicional.
            """;

    /**
     * Builds the user message embedding the scoring JSON.
     *
     * @param scoringJson the scoring data serialized to JSON
     * @return the user prompt text
     */
    public static String buildUserMessage(final String scoringJson) {
        return """
                Genera el informe de riesgo para la siguiente solicitud de crédito.
                Datos de scoring (JSON):
                """ + scoringJson;
    }

    /**
     * Builds the required OpenAPI validation response schema.
     * 
     * @return the response schema (OpenAPI subset) enforcing the report
     *         structure
     */
    public static Map<String, Object> responseSchema() {
        final Map<String, Object> riskFactorProps = new LinkedHashMap<>();
        riskFactorProps.put("factor", type("STRING"));
        riskFactorProps.put("severity", enumType("BAJO", "MEDIO", "ALTO"));
        riskFactorProps.put("description", type("STRING"));

        final Map<String, Object> riskFactorItem = new LinkedHashMap<>();
        riskFactorItem.put("type", "OBJECT");
        riskFactorItem.put("properties", riskFactorProps);
        riskFactorItem.put("required", List.of("factor", "severity", "description"));

        final Map<String, Object> riskFactors = new LinkedHashMap<>();
        riskFactors.put("type", "ARRAY");
        riskFactors.put("items", riskFactorItem);

        final Map<String, Object> recommendations = new LinkedHashMap<>();
        recommendations.put("type", "ARRAY");
        recommendations.put("items", type("STRING"));

        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("title", type("STRING"));
        properties.put("ai_summary", type("STRING"));
        properties.put("risk_analysis", type("STRING"));
        properties.put("risk_factors", riskFactors);
        properties.put("recommendations", recommendations);

        final Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "OBJECT");
        schema.put("properties", properties);
        schema.put("required",
                List.of("title", "ai_summary", "risk_analysis", "risk_factors", "recommendations"));
        schema.put("propertyOrdering",
                List.of("title", "ai_summary", "risk_analysis", "risk_factors", "recommendations"));
        return schema;
    }

    /**
     * Utility method creating a map specifying type.
     * 
     * @param type the type label
     * @return the schema details map
     */
    private static Map<String, Object> type(final String type) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", type);
        return map;
    }

    /**
     * Utility method creating a map specifying enum type values.
     * 
     * @param values the enum string choices
     * @return the schema details map
     */
    private static Map<String, Object> enumType(final String... values) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "STRING");
        map.put("enum", List.of(values));
        return map;
    }
}
