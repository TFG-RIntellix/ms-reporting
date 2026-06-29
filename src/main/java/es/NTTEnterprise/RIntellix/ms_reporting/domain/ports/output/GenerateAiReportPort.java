package es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;

/**
 * Output port for generating the natural-language risk report through an AI
 * model acting as a financial risk analyst.
 */
public interface GenerateAiReportPort {

    /**
     * Generates the AI report content for the given scoring data.
     *
     * @param scoringData the scoring data to analyze.
     * @return the structured AI report content.
     */
    AiReportContent generateReport(ScoringData scoringData);

    /**
     * @return the identifier/version of the AI model used (stored as the
     *         report's model_version).
     */
    String getModelName();
}
