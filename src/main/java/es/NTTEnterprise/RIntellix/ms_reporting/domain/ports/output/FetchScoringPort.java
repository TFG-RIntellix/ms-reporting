package es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;

/**
 * Output port to retrieve the scoring data (and related identifiers) from
 * ms-core-data for a given request.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
public interface FetchScoringPort {

    /**
     * Retrieves the scoring data for a request, including the scoringId and
     * partyId required by the report.
     *
     * @param requestId the request identifier coming from the Kafka message
     * @return the aggregated scoring data
     */
    ScoringData fetchScoringData(String requestId);
}
