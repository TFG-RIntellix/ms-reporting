package es.NTTEnterprise.RIntellix.ms_reporting.domain.entities;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single risk factor identified by the AI analyst in the risk report.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactor {

    private String factor;
    private Severity severity;
    private String description;
}
