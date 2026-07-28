package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.genai.types.Schema;
import com.google.genai.types.Type;

import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;

/**
 * Builds the prompt artefacts for the Gemini analyst agent: the system
 * instruction (persona), the user message and the JSON response schema used to
 * force structured output.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
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
        public static Schema responseSchema() {
                return Schema.builder()
                                .type(Type.Known.OBJECT)
                                .properties(Map.of(
                                                "title", Schema.builder().type(Type.Known.STRING).build(),
                                                "ai_summary", Schema.builder().type(Type.Known.STRING).build(),
                                                "risk_analysis", Schema.builder().type(Type.Known.STRING).build(),
                                                "risk_factors", Schema.builder()
                                                                .type(Type.Known.ARRAY)
                                                                .items(Schema.builder()
                                                                                .type(Type.Known.OBJECT)
                                                                                .properties(Map.of(
                                                                                                "factor",
                                                                                                Schema.builder().type(
                                                                                                                Type.Known.STRING)
                                                                                                                .build(),
                                                                                                "severity",
                                                                                                Schema.builder().type(
                                                                                                                Type.Known.STRING)
                                                                                                                .enum_(List.of(
                                                                                                                                "BAJO",
                                                                                                                                "MEDIO",
                                                                                                                                "ALTO"))
                                                                                                                .build(),
                                                                                                "description",
                                                                                                Schema.builder().type(
                                                                                                                Type.Known.STRING)
                                                                                                                .build()))
                                                                                .required(List.of("factor", "severity",
                                                                                                "description"))
                                                                                .build())
                                                                .build(),
                                                "recommendations", Schema.builder()
                                                                .type(Type.Known.ARRAY)
                                                                .items(Schema.builder().type(Type.Known.STRING).build())
                                                                .build()))
                                .required(List.of("title", "ai_summary", "risk_analysis", "risk_factors",
                                                "recommendations"))
                                .build();
        }
}
