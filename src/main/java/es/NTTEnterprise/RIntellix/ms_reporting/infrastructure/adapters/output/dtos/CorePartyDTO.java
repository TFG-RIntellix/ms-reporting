package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Mirror of ms-core-data's internal RequestPartyDTO, used to resolve the
 * {@code partyId} and {@code partyName} required by the report. These fields are
 * exposed by ms-core-data's internal {@code GET /api/requests/{id}/party}
 * endpoint. Unknown properties are ignored so the mapping stays
 * forward-compatible.
 *
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorePartyDTO {

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
