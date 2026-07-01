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
 * Carries the identifiers required by the report (scoringId, requestId,
 * partyId) plus the risk metrics and explainability that are sent to the AI
 * analyst to build the report.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringData {

    /**
     * Unique identifier of the computed scoring.
     */
    private String scoringId;

    /**
     * Unique identifier of the credit request.
     */
    private String requestId;

    /**
     * Unique identifier of the party requesting credit.
     */
    private String partyId;

    /**
     * Full name of the party requesting credit, used in the report title.
     */
    private String partyName;

    /**
     * Version of the ML model used to generate this scoring.
     */
    private String modelVersion;

    /**
     * Date when the scoring was generated.
     */
    private String scoringDate;

    /**
     * Raw inputs sent to the ML model.
     */
    private Map<String, Object> inputFeatures;

    /**
     * Probability of Default.
     */
    private Double pd;

    /**
     * Loss Given Default.
     */
    private Double lgd;

    /**
     * Exposure at Default.
     */
    private Double ead;

    /**
     * Expected Credit Loss.
     */
    private Double ecl;

    /**
     * Assigned risk grade based on PD.
     */
    private String riskGrade;

    /**
     * Monthly installment payment.
     */
    private Double monthlyPayment;

    /**
     * Debt-To-Income ratio.
     */
    private Double dti;

    /**
     * Total amount of payments scheduled.
     */
    private Double totalPayment;

    /**
     * Total amount of interest scheduled.
     */
    private Double totalInterest;

    /**
     * Monthly disposable income calculated.
     */
    private Double monthlyDisposableIncome;

    /**
     * Base prediction value of the ML model.
     */
    private Double baseValue;

    /**
     * List of top features explaining the scoring contribution.
     */
    private List<TopFeature> topFeatures;
}
