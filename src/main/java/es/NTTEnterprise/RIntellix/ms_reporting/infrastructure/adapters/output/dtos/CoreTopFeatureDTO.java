package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Mirror of ms-core-data's TopFeatureDTO (SHAP explainability).
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreTopFeatureDTO {

    /**
     * The name of the feature contributor.
     */
    private String featureName;

    /**
     * The actual value of the feature for the request.
     */
    private String featureValue;

    /**
     * The SHAP contribution value.
     */
    private Double shapValue;
}
