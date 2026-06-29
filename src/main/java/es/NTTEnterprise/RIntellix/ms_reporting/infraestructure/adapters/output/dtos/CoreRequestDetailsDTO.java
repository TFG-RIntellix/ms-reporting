package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Partial mirror of ms-core-data's RequestDetailsDTO, used only to resolve the
 * {@code partyId} required by the report.
 *
 * NOTE: ms-core-data does not expose {@code partyId} on this endpoint yet; this
 * field will be populated once that small change is made on the ms-core-data
 * side. Unknown properties are ignored so the mapping stays forward-compatible.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoreRequestDetailsDTO {

    private String requestId;
    private String partyId;
    private String partyName;
}
