package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.ScoringNotAvailableException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.FetchScoringPort;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.CoreRequestDetailsDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output.dtos.CoreScoringResponseDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.config.HttpClientConfig;
import es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.mappers.ScoringDataMapper;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that retrieves scoring data from ms-core-data.
 *
 * The scoring lookup doubles as the race-condition guard: while ms-core-data
 * has not yet persisted the scoring the endpoint returns 404, which is
 * translated into a {@link ScoringNotAvailableException} so the Kafka message is
 * retried.
 */
@Slf4j
@Component
public class MsCoreDataScoringAdapter implements FetchScoringPort {

    private static final String SCORING_PATH = "/api/requests/{requestId}/scoring";
    private static final String REQUEST_DETAILS_PATH = "/api/requests/{requestId}";

    private final RestClient restClient;

    public MsCoreDataScoringAdapter(
            @Qualifier(HttpClientConfig.MS_CORE_DATA_CLIENT) final RestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient);
    }

    @Override
    public ScoringData fetchScoringData(final String requestId) {
        log.info(LogMessage.SCORING_FETCH_START, requestId);

        final CoreScoringResponseDTO scoring = fetchScoring(requestId);
        final String partyId = resolvePartyId(requestId);

        return ScoringDataMapper.toDomain(scoring, partyId);
    }

    private CoreScoringResponseDTO fetchScoring(final String requestId) {
        try {
            return restClient.get()
                    .uri(SCORING_PATH, requestId)
                    .retrieve()
                    .body(CoreScoringResponseDTO.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ScoringNotAvailableException(
                        String.format(LogMessage.SCORING_NOT_AVAILABLE, requestId, ex.getStatusCode()), ex);
            }
            throw new ScoringNotAvailableException(
                    String.format(LogMessage.SCORING_FETCH_ERROR, requestId, ex.getStatusCode()), ex);
        }
    }

    private String resolvePartyId(final String requestId) {
        try {
            final CoreRequestDetailsDTO details = restClient.get()
                    .uri(REQUEST_DETAILS_PATH, requestId)
                    .retrieve()
                    .body(CoreRequestDetailsDTO.class);
            return details != null ? details.getPartyId() : null;
        } catch (RestClientResponseException ex) {
            log.warn(LogMessage.PARTY_RESOLVE_WARN, requestId);
            return null;
        }
    }
}
