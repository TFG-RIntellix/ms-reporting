package es.NTTEnterprise.RIntellix.ms_reporting.application.dtos.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Lean view of the persistScoring Kafka message published by ms-risk-engine and
 * also consumed by ms-core-data.
 *
 * ms-reporting only needs the {@code requestId} to look up the authoritative
 * scoring (and its scoringId / partyId) from ms-core-data, so all other fields
 * of the message are ignored.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoringResultMessageDTO {

    @NotNull(message = "Request ID is required")
    @NotBlank(message = "Request ID cannot be blank")
    private String requestId;

    private String modelVersion;
}
