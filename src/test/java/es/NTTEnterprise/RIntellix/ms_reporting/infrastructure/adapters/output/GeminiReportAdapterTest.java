package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.AiReportGenerationException;

/**
 * Unit tests for {@link GeminiReportAdapter}.
 * Covers successful report generation, empty response handling, serialization
 * errors,
 * API call errors, response parsing errors, and model name retrieval.
 */
@DisplayName("GeminiReportAdapter Tests")
@ExtendWith(MockitoExtension.class)
class GeminiReportAdapterTest {

        @Mock
        private Client genaiClient;

        @Mock
        private Models models;

        @Mock
        private ObjectMapper objectMapper;

        private static final String MODEL = "gemini-2.0-flash";

        private GeminiReportAdapter adapter;

        @BeforeEach
        void setUp() {
                org.springframework.test.util.ReflectionTestUtils.setField(genaiClient, "models", models);
                adapter = new GeminiReportAdapter(genaiClient, objectMapper, MODEL);
        }

        // ========== getModelName() ==========

        @Test
        @DisplayName("Should return configured model name")
        void getModelName_returnsConfiguredModel() {
                assertEquals(MODEL, adapter.getModelName());
        }

        // ========== generateReport() — success ==========

        @Test
        @DisplayName("Should successfully generate report and map it to domain")
        void generateReport_success() throws Exception {
                ScoringData scoringData = ScoringData.builder()
                                .scoringId("SCO-1")
                                .requestId("REQ-1")
                                .partyId("P-1")
                                .build();

                String fakeJsonResponse = "{\"summary\":\"Test summary\",\"risk_factors\":[],\"recommendations\":[]}";
                
                when(objectMapper.writeValueAsString(scoringData))
                                .thenReturn("{\"scoringId\":\"SCO-1\"}");

                GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
                when(mockResponse.text()).thenReturn(fakeJsonResponse);
                when(models.generateContent(eq(MODEL), any(Content.class), any(GenerateContentConfig.class)))
                                .thenReturn(mockResponse);

                es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers.GeminiReportMapper.GeminiReportPayload fakePayload = 
                        new es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers.GeminiReportMapper.GeminiReportPayload(
                                "Test title", "Test summary", "Test analysis", java.util.List.of(), java.util.List.of());
                
                when(objectMapper.readValue(eq(fakeJsonResponse), eq(es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers.GeminiReportMapper.GeminiReportPayload.class)))
                                .thenReturn(fakePayload);

                es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent result = adapter.generateReport(scoringData);

                assertNotNull(result);
                assertEquals("Test summary", result.getAiSummary());
        }

        // ========== generateReport() — serialization failure ==========
        @Test
        @DisplayName("Should throw AiReportGenerationException when scoring serialization fails")
        void generateReport_serializationFails_throwsException() throws JsonProcessingException {
                ScoringData scoringData = ScoringData.builder()
                                .scoringId("SCO-1")
                                .requestId("REQ-1")
                                .partyId("P-1")
                                .build();

                when(objectMapper.writeValueAsString(scoringData))
                                .thenThrow(new JsonProcessingException("Serialization error") {
                                });

                assertThrows(AiReportGenerationException.class,
                                () -> adapter.generateReport(scoringData));
        }

        // ========== generateReport() — API call failure ==========

        @Test
        @DisplayName("Should throw AiReportGenerationException when Gemini API call fails")
        void generateReport_apiCallFails_throwsException() throws Exception {
                ScoringData scoringData = ScoringData.builder()
                                .scoringId("SCO-1")
                                .requestId("REQ-1")
                                .partyId("P-1")
                                .build();

                when(objectMapper.writeValueAsString(scoringData))
                                .thenReturn("{\"scoringId\":\"SCO-1\"}");
                when(models.generateContent(eq(MODEL), any(Content.class), any(GenerateContentConfig.class)))
                                .thenThrow(new RuntimeException("API timeout"));

                AiReportGenerationException exception = assertThrows(AiReportGenerationException.class,
                                () -> adapter.generateReport(scoringData));

                assertTrue(exception.getMessage().contains("REQ-1"),
                                "Exception message should contain the request ID for traceability");
        }

        // ========== generateReport() — empty response ==========

        @Test
        @DisplayName("Should throw AiReportGenerationException when Gemini returns empty text")
        void generateReport_emptyResponse_throwsException() throws Exception {
                ScoringData scoringData = ScoringData.builder()
                                .scoringId("SCO-1")
                                .requestId("REQ-1")
                                .partyId("P-1")
                                .build();

                when(objectMapper.writeValueAsString(scoringData))
                                .thenReturn("{\"scoringId\":\"SCO-1\"}");

                GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
                when(mockResponse.text()).thenReturn("");
                when(models.generateContent(eq(MODEL), any(Content.class), any(GenerateContentConfig.class)))
                                .thenReturn(mockResponse);

                assertThrows(AiReportGenerationException.class,
                                () -> adapter.generateReport(scoringData));
        }

        @Test
        @DisplayName("Should throw AiReportGenerationException when Gemini returns null text")
        void generateReport_nullResponse_throwsException() throws Exception {
                ScoringData scoringData = ScoringData.builder()
                                .scoringId("SCO-1")
                                .requestId("REQ-1")
                                .partyId("P-1")
                                .build();

                when(objectMapper.writeValueAsString(scoringData))
                                .thenReturn("{\"scoringId\":\"SCO-1\"}");

                GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
                when(mockResponse.text()).thenReturn(null);
                when(models.generateContent(eq(MODEL), any(Content.class), any(GenerateContentConfig.class)))
                                .thenReturn(mockResponse);

                assertThrows(AiReportGenerationException.class,
                                () -> adapter.generateReport(scoringData));
        }

        // ========== generateReport() — parse failure ==========

        @Test
        @DisplayName("Should throw AiReportGenerationException when response JSON parsing fails")
        void generateReport_parseFails_throwsException() throws Exception {
                ScoringData scoringData = ScoringData.builder()
                                .scoringId("SCO-1")
                                .requestId("REQ-1")
                                .partyId("P-1")
                                .build();

                when(objectMapper.writeValueAsString(scoringData))
                                .thenReturn("{\"scoringId\":\"SCO-1\"}");

                GenerateContentResponse mockResponse = mock(GenerateContentResponse.class);
                when(mockResponse.text()).thenReturn("{invalid json}");
                when(models.generateContent(eq(MODEL), any(Content.class), any(GenerateContentConfig.class)))
                                .thenReturn(mockResponse);
                when(objectMapper.readValue(eq("{invalid json}"), any(Class.class)))
                                .thenThrow(new JsonProcessingException("Parse error") {
                                });

                assertThrows(AiReportGenerationException.class,
                                () -> adapter.generateReport(scoringData));
        }

        // ========== Constructor null guards ==========

        @Test
        @DisplayName("Constructor should throw NPE when genaiClient is null")
        void constructor_nullClient_throwsNPE() {
                assertThrows(NullPointerException.class,
                                () -> new GeminiReportAdapter(null, objectMapper, MODEL));
        }

        @Test
        @DisplayName("Constructor should throw NPE when objectMapper is null")
        void constructor_nullObjectMapper_throwsNPE() {
                assertThrows(NullPointerException.class,
                                () -> new GeminiReportAdapter(genaiClient, null, MODEL));
        }

        @Test
        @DisplayName("Constructor should throw NPE when model is null")
        void constructor_nullModel_throwsNPE() {
                assertThrows(NullPointerException.class,
                                () -> new GeminiReportAdapter(genaiClient, objectMapper, null));
        }
}
