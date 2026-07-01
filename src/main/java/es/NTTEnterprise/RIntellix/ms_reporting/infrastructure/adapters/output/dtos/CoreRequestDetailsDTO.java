package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Partial mirror of ms-core-data's RequestDetailsDTO, used to resolve the
 * {@code partyId} and {@code partyName} required by the report. Both fields are
 * exposed by ms-core-data's {@code GET /api/requests/{id}} endpoint. Unknown
 * properties are ignored so the mapping stays forward-compatible.
 *
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreRequestDetailsDTO {

    /**
     * Unique request identifier.
     */
    private String requestId;

    /**
     * Unique party identifier.
     */
    private String partyId;

    /**
     * Full name of the associated party.
     */
    private String partyName;
}
