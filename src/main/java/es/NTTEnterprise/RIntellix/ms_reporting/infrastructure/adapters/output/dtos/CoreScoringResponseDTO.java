package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Mirror of the response returned by ms-core-data's
 * {@code GET /api/requests/{requestId}/scoring} endpoint (ScoringDTO).
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreScoringResponseDTO {

    /**
     * Unique computed scoring identifier.
     */
    private String scoringId;

    /**
     * Associated request identifier.
     */
    private String requestId;

    /**
     * Version of the ML model used to generate scoring.
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
     * List of top contributing features (explainability).
     */
    private List<CoreTopFeatureDTO> topFeatures;
}
