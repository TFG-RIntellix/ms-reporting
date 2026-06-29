package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.mappers;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.TopFeature;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.CoreScoringResponseDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.CoreTopFeatureDTO;

/**
 * Maps ms-core-data scoring responses into the {@link ScoringData} domain
 * entity, enriching it with the resolved partyId.
 */
public final class ScoringDataMapper {

    private ScoringDataMapper() {
    }

    public static ScoringData toDomain(final CoreScoringResponseDTO dto, final String partyId) {
        Objects.requireNonNull(dto, "scoring response is required");

        final List<TopFeature> topFeatures = dto.getTopFeatures() == null ? List.of()
                : dto.getTopFeatures().stream()
                        .map(ScoringDataMapper::toTopFeature)
                        .toList();

        return ScoringData.builder()
                .scoringId(dto.getScoringId())
                .requestId(dto.getRequestId())
                .partyId(partyId)
                .modelVersion(dto.getModelVersion())
                .scoringDate(dto.getScoringDate())
                .inputFeatures(dto.getInputFeatures())
                .pd(dto.getPd())
                .lgd(dto.getLgd())
                .ead(dto.getEad())
                .ecl(dto.getEcl())
                .riskGrade(dto.getRiskGrade())
                .monthlyPayment(dto.getMonthlyPayment())
                .dti(dto.getDti())
                .totalPayment(dto.getTotalPayment())
                .totalInterest(dto.getTotalInterest())
                .monthlyDisposableIncome(dto.getMonthlyDisposableIncome())
                .baseValue(dto.getBaseValue())
                .topFeatures(topFeatures)
                .build();
    }

    private static TopFeature toTopFeature(final CoreTopFeatureDTO dto) {
        return TopFeature.builder()
                .featureName(dto.getFeatureName())
                .featureValue(dto.getFeatureValue())
                .shapValue(dto.getShapValue())
                .build();
    }
}
