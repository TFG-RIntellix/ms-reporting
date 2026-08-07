package es.NTTEnterprise.RIntellix.ms_reporting.domain.entities;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single risk factor identified by the AI analyst in the risk report.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactor {

    /**
     * The name or title of the risk factor.
     */
    private String factor;

    /**
     * The severity level of this risk factor.
     */
    private Severity severity;

    /**
     * Detailed explanation of the risk factor.
     */
    private String description;
}
