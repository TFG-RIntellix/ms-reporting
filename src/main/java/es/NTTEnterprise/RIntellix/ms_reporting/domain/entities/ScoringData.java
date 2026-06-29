package es.NTTEnterprise.RIntellix.ms_reporting.domain.entities;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aggregated scoring information retrieved from ms-core-data for a request.
 *
 * Carries the identifiers required by the report (scoringId, requestId,
 * partyId) plus the risk metrics and explainability that are sent to the AI
 * analyst to build the report.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringData {

    private String scoringId;
    private String requestId;
    private String partyId;
    private String modelVersion;
    private String scoringDate;
    private Map<String, Object> inputFeatures;
    private Double pd;
    private Double lgd;
    private Double ead;
    private Double ecl;
    private String riskGrade;
    private Double monthlyPayment;
    private Double dti;
    private Double totalPayment;
    private Double totalInterest;
    private Double monthlyDisposableIncome;
    private Double baseValue;
    private List<TopFeature> topFeatures;
}
