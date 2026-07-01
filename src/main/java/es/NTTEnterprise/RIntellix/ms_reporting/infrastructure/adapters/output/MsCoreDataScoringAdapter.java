package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.ScoringNotAvailableException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.FetchScoringPort;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.CorePartyDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.CoreScoringResponseDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.config.HttpClientConfig;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers.ScoringDataMapper;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that retrieves scoring data from ms-core-data.
 * The scoring lookup doubles as the race-condition guard: while ms-core-data
 * has not yet persisted the scoring the endpoint returns 404, which is
 * translated into a {@link ScoringNotAvailableException} so the Kafka message is
 * retried.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Slf4j
@Component
public class MsCoreDataScoringAdapter implements FetchScoringPort {

    private final RestClient restClient;

    /**
     * Constructs an MsCoreDataScoringAdapter with the required rest client.
     *
     * @param restClient the RestClient qualified for ms-core-data
     */
    public MsCoreDataScoringAdapter(
            @Qualifier(HttpClientConfig.MS_CORE_DATA_CLIENT) final RestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient);
    }

    @Override
    @Retryable(value = ScoringNotAvailableException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 2))
    public ScoringData fetchScoringData(final String requestId) {
        log.info(LogMessage.SCORING_FETCH_START, requestId);

        final CoreScoringResponseDTO scoring = fetchScoring(requestId);
        final CorePartyDTO party = resolveRequestParty(requestId);
        final String partyId = party != null ? party.getPartyId() : null;
        final String partyName = party != null ? party.getPartyName() : null;

        return ScoringDataMapper.toDomain(scoring, partyId, partyName);
    }

    /**
     * Calls ms-core-data API to fetch raw scoring.
     *
     * @param requestId the request ID to query
     * @return the CoreScoringResponseDTO containing scoring details
     */
    private CoreScoringResponseDTO fetchScoring(final String requestId) {
        try {
            return restClient.get()
                    .uri(ReportConstants.MS_CORE_DATA_SCORING_PATH, requestId)
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

    /**
     * Resolves the party reference (partyId and partyName) from ms-core-data's
     * internal request party API.
     *
     * @param requestId the request ID to query
     * @return the associated party reference, or null if not resolved
     */
    private CorePartyDTO resolveRequestParty(final String requestId) {
        try {
            return restClient.get()
                    .uri(ReportConstants.MS_CORE_DATA_REQUEST_PARTY_PATH, requestId)
                    .retrieve()
                    .body(CorePartyDTO.class);
        } catch (RestClientResponseException ex) {
            log.warn(LogMessage.PARTY_RESOLVE_WARN, requestId);
            return null;
        }
    }
}
