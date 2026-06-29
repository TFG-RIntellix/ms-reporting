package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import java.util.List;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.gemini.GeminiReportPayload;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;

/**
 * Maps the Gemini JSON response payload to the domain {@link AiReportContent} aggregate.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
public final class GeminiReportMapper {

    private GeminiReportMapper() {
        throw new UnsupportedOperationException(ReportConstants.PDF_HYPHEN);
    }

    /**
     * Translates the GeminiReportPayload DTO into AiReportContent domain entity.
     *
     * @param payload the parsed JSON report details
     * @return the constructed AiReportContent domain entity
     */
    public static AiReportContent toDomain(final GeminiReportPayload payload) {
        if (payload == null) {
            return null;
        }

        final List<RiskFactor> riskFactors = payload.riskFactors() == null ? List.of()
                : payload.riskFactors().stream()
                        .map(rf -> RiskFactor.builder()
                                .factor(rf.factor())
                                .severity(toSeverity(rf.severity()))
                                .description(rf.description())
                                .build())
                        .toList();

        return AiReportContent.builder()
                .title(payload.title())
                .aiSummary(payload.aiSummary())
                .riskAnalysis(payload.riskAnalysis())
                .riskFactors(riskFactors)
                .recommendations(payload.recommendations() == null ? List.of() : payload.recommendations())
                .build();
    }

    /**
     * Normalizes the severity string into a Severity enum value.
     *
     * @param value raw severity string
     * @return normalized Severity enum value, defaults to MEDIO
     */
    private static Severity toSeverity(final String value) {
        if (value == null) {
            return Severity.MEDIO;
        }
        try {
            return Severity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Severity.MEDIO;
        }
    }
}
