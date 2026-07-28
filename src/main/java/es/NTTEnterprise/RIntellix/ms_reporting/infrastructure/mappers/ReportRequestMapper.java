package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.ReportRequestDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;

/**
 * Maps the {@link Report} domain aggregate into the {@link ReportRequestDTO}
 * sent to ms-core-data.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
public final class ReportRequestMapper {

    private ReportRequestMapper() {
        throw new UnsupportedOperationException(ReportConstants.REPORT_REQUIRED_MSG);
    }

    /**
     * Translates a Report domain entity into a ReportRequestDTO.
     *
     * @param report the domain entity to map
     * @return the constructed DTO
     */
    public static ReportRequestDTO toRequest(final Report report) {
        Objects.requireNonNull(report, ReportConstants.REPORT_REQUIRED_MSG);

        final List<ReportRequestDTO.RiskFactorDTO> riskFactors = report.getRiskFactors() == null ? List.of()
                : report.getRiskFactors().stream()
                        .map(ReportRequestMapper::toRiskFactorDTO)
                        .toList();

        return ReportRequestDTO.builder()
                .partyId(report.getPartyId())
                .requestId(report.getRequestId())
                .scoringId(report.getScoringId())
                .reportType(report.getReportType() != null ? report.getReportType().name() : null)
                .title(report.getTitle())
                .aiSummary(report.getAiSummary())
                .riskAnalysis(report.getRiskAnalysis())
                .riskFactors(riskFactors)
                .recommendations(report.getRecommendations())
                .filePath(report.getFilePath())
                .fileSizeBytes(report.getFileSizeBytes())
                .generatedBy(report.getGeneratedBy())
                .generatedDate(report.getGeneratedDate())
                .generationTimeMs(report.getGenerationTimeMs())
                .modelVersion(report.getModelVersion())
                .language(report.getLanguage())
                .build();
    }

    /**
     * Translates a RiskFactor domain entity into a static nested RiskFactorDTO.
     *
     * @param factor the domain risk factor details
     * @return the constructed inner RiskFactorDTO
     */
    private static ReportRequestDTO.RiskFactorDTO toRiskFactorDTO(final RiskFactor factor) {
        return ReportRequestDTO.RiskFactorDTO.builder()
                .factor(factor.getFactor())
                .severity(factor.getSeverity() != null ? factor.getSeverity().name() : null)
                .description(factor.getDescription())
                .build();
    }
}
