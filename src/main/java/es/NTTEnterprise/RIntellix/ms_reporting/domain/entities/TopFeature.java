package es.NTTEnterprise.RIntellix.ms_reporting.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A SHAP top contributing feature of the scoring, used as part of the AI input.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopFeature {

    private String featureName;
    private String featureValue;
    private Double shapValue;
}
