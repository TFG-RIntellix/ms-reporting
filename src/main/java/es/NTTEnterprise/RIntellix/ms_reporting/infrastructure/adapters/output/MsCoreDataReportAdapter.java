package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.ReportPersistenceException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.StoreReportPort;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.ReportRequestDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.config.HttpClientConfig;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers.ReportRequestMapper;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that persists the report by calling ms-core-data's
 * {@code POST /api/reports} endpoint.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Slf4j
@Component
public class MsCoreDataReportAdapter implements StoreReportPort {

    private final RestClient restClient;

    /**
     * Constructs an MsCoreDataReportAdapter with the required rest client.
     *
     * @param restClient the RestClient qualified for ms-core-data
     */
    public MsCoreDataReportAdapter(
            @Qualifier(HttpClientConfig.MS_CORE_DATA_CLIENT) final RestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient);
    }

    @Override
    public void store(final Report report) {
        log.info(LogMessage.REPORT_POST_START, report.getRequestId());
        final ReportRequestDTO body = ReportRequestMapper.toRequest(report);

        try {
            restClient.post()
                    .uri(ReportConstants.MS_CORE_DATA_REPORTS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new ReportPersistenceException(
                    String.format(LogMessage.REPORT_POST_ERROR, report.getRequestId(), ex.getStatusCode()), ex);
        } catch (RestClientException ex) {
            throw new ReportPersistenceException(
                    String.format(LogMessage.REPORT_POST_ERROR, report.getRequestId(), ReportConstants.NOT_AVAILABLE_STATUS), ex);
        }
    }
}
