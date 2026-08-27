package es.NTTEnterprise.RIntellix.ms_reporting.domain.factories;

import java.util.Date;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.ReportType;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;

/**
 * Domain factory responsible for assembling the {@link Report} aggregate
 * from various data sources (Scoring, AI output).
 * 
 * @author Lucía Fernández Mancebo
 * @date 20/08/2026
 */
public final class ReportFactory {

    private ReportFactory() {
        throw new UnsupportedOperationException(ReportConstants.PDF_HYPHEN);
    }

    /**
     * Builds the Report aggregate entity from the scoring and AI generated content.
     *
     * @param scoringData the scoring source data.
     * @param aiContent   the AI generated content.
     * @param modelName   the AI model version used.
     * @return the constructed Report.
     */
    public static Report createReport(final ScoringData scoringData, final AiReportContent aiContent, final String modelName) {
        return Report.builder()
                .partyId(scoringData.getPartyId())
                .requestId(scoringData.getRequestId())
                .scoringId(scoringData.getScoringId())
                .reportType(ReportType.RISK_ANALYSIS)
                .title(buildTitle(scoringData, aiContent))
                .aiSummary(aiContent.getAiSummary())
                .riskAnalysis(aiContent.getRiskAnalysis())
                .riskFactors(aiContent.getRiskFactors())
                .recommendations(aiContent.getRecommendations())
                .generatedBy(ReportConstants.GENERATED_BY)
                .generatedDate(new Date())
                .modelVersion(modelName)
                .language(ReportConstants.LANGUAGE_SPANISH)
                .scoringData(scoringData)
                .build();
    }

    /**
     * Builds the report title using the party name, e.g.
     * {@code "Informe de Evaluación de Riesgo de Crédito - <partyName>"}.
     * Falls back to the AI-generated title when the party name is not available.
     *
     * @param scoringData the scoring source data.
     * @param aiContent   the AI generated content.
     * @return the constructed title.
     */
    private static String buildTitle(final ScoringData scoringData, final AiReportContent aiContent) {
        final String partyName = scoringData.getPartyName();
        if (partyName != null && !partyName.isBlank()) {
            return ReportConstants.REPORT_TITLE_PREFIX + partyName.trim();
        }
        return aiContent.getTitle();
    }
}
