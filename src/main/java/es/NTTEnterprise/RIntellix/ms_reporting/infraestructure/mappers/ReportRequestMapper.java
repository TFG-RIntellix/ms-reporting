package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.mappers;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.ReportRequestDTO;

/**
 * Maps the {@link Report} domain aggregate into the {@link ReportRequestDTO}
 * sent to ms-core-data.
 */
public final class ReportRequestMapper {

    private ReportRequestMapper() {
    }

    public static ReportRequestDTO toRequest(final Report report) {
        Objects.requireNonNull(report, "report is required");

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

    private static ReportRequestDTO.RiskFactorDTO toRiskFactorDTO(final RiskFactor factor) {
        return ReportRequestDTO.RiskFactorDTO.builder()
                .factor(factor.getFactor())
                .severity(factor.getSeverity() != null ? factor.getSeverity().name() : null)
                .description(factor.getDescription())
                .build();
    }
}
