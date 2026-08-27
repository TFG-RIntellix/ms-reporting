package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.util;

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
   * that always answers in Spanish with a strict JSON object, grounded in the
   * full scoring payload received (metrics, financial ratios and SHAP
   * explainability).
   */
  public static final String SYSTEM_INSTRUCTION = """
      Actúa como un analista sénior de riesgo de crédito de una entidad bancaria, redactando la
      sección narrativa de un informe de scoring que un comité de riesgos revisará. Recibirás un objeto JSON de
      scoring con estos bloques de datos:

      - Métricas regulatorias: probability_of_default (PD), lgd, ead, ecl, risk_grade.
      - Métricas financieras del solicitante: dti (ratio deuda/ingresos), monthly_payment,
        monthly_disposable_income, total_payment, total_interest, y los datos brutos de la
        solicitud en input_features (importe, plazo, tipo de interés, ltv si aplica, finalidad...).
      - Explicabilidad del modelo: base_value y top_features (lista de variables SHAP con su
        feature_value y shap_value; un shap_value positivo empuja el PD al alza, uno negativo lo
        reduce).

      Requisitos de contenido:
      - Redacta SIEMPRE en español, con un tono profesional, humano y analítico. Escribe como si fueras
        un analista explicando el caso a otro colega o al comité.
      - NO utilices jerga matemática ni menciones directamente conceptos del modelo algorítmico como
        "valores SHAP", "base_value", "variables" o "shap_value". Traduce la información matemática a un
        lenguaje natural de negocio (por ejemplo, en lugar de decir "la variable edad tiene un shap_value
        negativo", di "la edad madura del solicitante actúa como un factor mitigante del riesgo").
      - "ai_summary" (1 párrafo, 80-120 palabras): resumen ejecutivo detallado en lenguaje natural que mencione la
        Probabilidad de Default (PD), el nivel de riesgo (risk_grade) y la pérdida esperada (ECL) de forma fluida,
        junto a una justificación rigurosa de la conclusión operativa (aprobar, revisar o rechazar).
      - "risk_analysis" (3-4 párrafos, 250-400 palabras en total): análisis narrativo profundo y detallado, pensado
        para una auditoría posterior. Explica de forma lógica y exhaustiva cómo los distintos aspectos del
        perfil del cliente (top_features) están impactando positiva o negativamente en el riesgo, relacionándolo
        de forma lógica con su capacidad de pago (DTI, ingresos disponibles). Proporciona el razonamiento completo y evita hacer listas de variables.
      - "risk_factors": entre 3 y 5 factores. Nombra el aspecto del perfil de forma natural (ej. "Nivel de Endeudamiento"
        en vez de "dti"). La "severity" debe ser (BAJO, MEDIO o ALTO). En "description" proporciona una explicación
        detallada, formal y auditable del impacto de este factor concreto y cita los datos relevantes que lo sostienen.
      - "recommendations": entre 3 y 5 acciones accionables, vinculadas a los risk_factors (p. ej. solicitar
        garantías adicionales, reducir importe, aportar más justificantes de ingresos).
      - Los importes se expresan en euros y los ratios en porcentaje con hasta dos decimales, en
        formato coherente con es-ES (p. ej. "38,50%", "1.250,00 €").
      - Devuelve EXCLUSIVAMENTE un objeto JSON válido que cumpla el esquema indicado, sin texto
        adicional, sin markdown y sin explicaciones fuera del JSON.
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
