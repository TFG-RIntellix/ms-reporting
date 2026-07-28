package es.NTTEnterprise.RIntellix.ms_reporting.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A SHAP top contributing feature of the scoring, used as part of the AI input.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopFeature {

    /**
     * The name of the feature from the ML model.
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
