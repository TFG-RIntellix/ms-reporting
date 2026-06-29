package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Mirror of the response returned by ms-core-data's
 * {@code GET /api/requests/{requestId}/scoring} endpoint (ScoringDTO).
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreScoringResponseDTO {

    private String scoringId;
    private String requestId;
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
    private List<CoreTopFeatureDTO> topFeatures;
}
