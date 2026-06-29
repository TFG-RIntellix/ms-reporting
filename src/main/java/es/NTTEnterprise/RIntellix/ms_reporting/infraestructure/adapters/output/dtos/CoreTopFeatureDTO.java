package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Mirror of ms-core-data's TopFeatureDTO (SHAP explainability).
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreTopFeatureDTO {

    private String featureName;
    private String featureValue;
    private Double shapValue;
}
