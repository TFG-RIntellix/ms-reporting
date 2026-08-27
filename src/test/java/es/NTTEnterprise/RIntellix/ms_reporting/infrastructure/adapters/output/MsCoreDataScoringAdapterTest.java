package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.ScoringNotAvailableException;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.CorePartyDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.CoreScoringResponseDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;

@ExtendWith(MockitoExtension.class)
class MsCoreDataScoringAdapterTest {

    @Mock
    private RestClient restClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private MsCoreDataScoringAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MsCoreDataScoringAdapter(restClient);
    }

    @Test
    @DisplayName("Should fetch scoring data successfully")
    void fetchScoringData_success() {
        // Mock fetchScoring
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(ReportConstants.MS_CORE_DATA_SCORING_PATH), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        CoreScoringResponseDTO scoringResponse = new CoreScoringResponseDTO();
        scoringResponse.setRequestId("REQ-1");
        
        when(responseSpec.body(CoreScoringResponseDTO.class)).thenReturn(scoringResponse);
        
        // Mock resolveRequestParty - this needs careful mocking because we use the same restClient.get() chain
        // In Mockito, when calling the same method with different args, we can use eq() matching
        
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec partyHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec partyResponseSpec = mock(RestClient.ResponseSpec.class);
        
        when(requestHeadersUriSpec.uri(eq(ReportConstants.MS_CORE_DATA_REQUEST_PARTY_PATH), anyString())).thenReturn(partyHeadersSpec);
        when(partyHeadersSpec.retrieve()).thenReturn(partyResponseSpec);
        
        CorePartyDTO partyResponse = new CorePartyDTO();
        partyResponse.setPartyId("P-1");
        partyResponse.setPartyName("John Doe");
        
        when(partyResponseSpec.body(CorePartyDTO.class)).thenReturn(partyResponse);

        ScoringData result = adapter.fetchScoringData("REQ-1");

        assertNotNull(result);
        assertEquals("P-1", result.getPartyId());
        assertEquals("John Doe", result.getPartyName());
    }

    @Test
    @DisplayName("Should throw ScoringNotAvailableException on 404")
    void fetchScoringData_throws404() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(ReportConstants.MS_CORE_DATA_SCORING_PATH), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        RestClientResponseException exception = mock(RestClientResponseException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(responseSpec.body(CoreScoringResponseDTO.class)).thenThrow(exception);

        ScoringNotAvailableException ex = assertThrows(ScoringNotAvailableException.class, () -> adapter.fetchScoringData("REQ-1"));
        assertTrue(ex.getMessage().contains("404"));
    }

    @Test
    @DisplayName("Should throw ScoringNotAvailableException on other errors")
    void fetchScoringData_throwsOtherError() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(ReportConstants.MS_CORE_DATA_SCORING_PATH), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        RestClientResponseException exception = mock(RestClientResponseException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(responseSpec.body(CoreScoringResponseDTO.class)).thenThrow(exception);

        ScoringNotAvailableException ex = assertThrows(ScoringNotAvailableException.class, () -> adapter.fetchScoringData("REQ-1"));
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    @DisplayName("Should handle missing party data gracefully")
    void fetchScoringData_missingParty() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(ReportConstants.MS_CORE_DATA_SCORING_PATH), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        CoreScoringResponseDTO scoringResponse = new CoreScoringResponseDTO();
        when(responseSpec.body(CoreScoringResponseDTO.class)).thenReturn(scoringResponse);
        
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersSpec partyHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec partyResponseSpec = mock(RestClient.ResponseSpec.class);
        
        when(requestHeadersUriSpec.uri(eq(ReportConstants.MS_CORE_DATA_REQUEST_PARTY_PATH), anyString())).thenReturn(partyHeadersSpec);
        when(partyHeadersSpec.retrieve()).thenReturn(partyResponseSpec);
        
        when(partyResponseSpec.body(CorePartyDTO.class)).thenThrow(mock(RestClientResponseException.class));

        ScoringData result = adapter.fetchScoringData("REQ-1");

        assertNotNull(result);
        assertNull(result.getPartyId());
        assertNull(result.getPartyName());
    }
}
