package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.ReportPersistenceException;

@ExtendWith(MockitoExtension.class)
class MsCoreDataReportAdapterTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private MsCoreDataReportAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MsCoreDataReportAdapter(restClient);
    }

    @Test
    @DisplayName("Should store report successfully")
    void store_success() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        Report report = new Report();
        report.setRequestId("REQ-123");

        assertDoesNotThrow(() -> adapter.store(report));

        verify(restClient).post();
        verify(requestBodySpec).retrieve();
    }

    @Test
    @DisplayName("Should throw ReportPersistenceException on RestClientResponseException")
    void store_throwsRestClientResponseException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        
        RestClientResponseException exception = mock(RestClientResponseException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(responseSpec.toBodilessEntity()).thenThrow(exception);

        Report report = new Report();
        report.setRequestId("REQ-123");

        ReportPersistenceException ex = assertThrows(ReportPersistenceException.class, () -> adapter.store(report));
        assertTrue(ex.getMessage().contains("400 BAD_REQUEST"));
    }

    @Test
    @DisplayName("Should throw ReportPersistenceException on general RestClientException")
    void store_throwsGeneralRestClientException() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        
        RestClientException exception = new RestClientException("Connection error");
        when(responseSpec.toBodilessEntity()).thenThrow(exception);

        Report report = new Report();
        report.setRequestId("REQ-123");

        ReportPersistenceException ex = assertThrows(ReportPersistenceException.class, () -> adapter.store(report));
        assertTrue(ex.getMessage().contains("N/A"));
    }
}
