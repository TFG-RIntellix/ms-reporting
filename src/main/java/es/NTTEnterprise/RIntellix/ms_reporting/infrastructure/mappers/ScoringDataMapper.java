package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import java.util.List;
import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.TopFeature;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.CoreScoringResponseDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.CoreTopFeatureDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;

/**
 * Maps ms-core-data scoring responses into the {@link ScoringData} domain
 * entity, enriching it with the resolved partyId and partyName.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
public final class ScoringDataMapper {

    private ScoringDataMapper() {
        throw new UnsupportedOperationException(ReportConstants.SCORING_RESPONSE_REQUIRED_MSG);
    }

    /**
     * Translates the CoreScoringResponseDTO combined with partyId and partyName
     * into ScoringData.
     *
     * @param dto       the scoring response DTO source
     * @param partyId   the associated party identifier
     * @param partyName the associated party full name
     * @return the constructed ScoringData domain entity
     */
    public static ScoringData toDomain(final CoreScoringResponseDTO dto, final String partyId,
            final String partyName) {
        Objects.requireNonNull(dto, ReportConstants.SCORING_RESPONSE_REQUIRED_MSG);

        final List<TopFeature> topFeatures = dto.getTopFeatures() == null ? List.of()
                : dto.getTopFeatures().stream()
                        .map(ScoringDataMapper::toTopFeature)
                        .toList();

        return ScoringData.builder()
                .scoringId(dto.getScoringId())
                .requestId(dto.getRequestId())
                .partyId(partyId)
                .partyName(partyName)
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

    /**
     * Translates CoreTopFeatureDTO into TopFeature domain entity.
     *
     * @param dto the top feature DTO details
     * @return the constructed TopFeature domain entity
     */
    private static TopFeature toTopFeature(final CoreTopFeatureDTO dto) {
        return TopFeature.builder()
                .featureName(dto.getFeatureName())
                .featureValue(dto.getFeatureValue())
                .shapValue(dto.getShapValue())
                .build();
    }
}
