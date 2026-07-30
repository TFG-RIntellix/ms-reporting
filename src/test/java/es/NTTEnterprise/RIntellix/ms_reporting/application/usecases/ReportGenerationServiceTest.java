package es.NTTEnterprise.RIntellix.ms_reporting.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RenderedPdf;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.ReportType;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.ScoringNotAvailableException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.FetchScoringPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.GenerateAiReportPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.RenderReportPdfPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.StoreReportPort;

@ExtendWith(MockitoExtension.class)
class ReportGenerationServiceTest {

    private static final String REQUEST_ID = "req-1";
    private static final String SCORING_ID = "sco-1";
    private static final String PARTY_ID = "party-1";
    private static final String PARTY_NAME = "Juan Pérez García";
    private static final String MODEL = "gemini-2.0-flash";

    @Mock
    private FetchScoringPort fetchScoringPort;
    @Mock
    private GenerateAiReportPort generateAiReportPort;
    @Mock
    private RenderReportPdfPort renderReportPdfPort;
    @Mock
    private StoreReportPort storeReportPort;

    private ReportGenerationService service;

    @BeforeEach
    void setUp() {
        service = new ReportGenerationService(
                fetchScoringPort, generateAiReportPort, renderReportPdfPort, storeReportPort);
    }

    @Test
    void shouldBuildAndPersistReportFromPipeline() {
        final ScoringData scoringData = ScoringData.builder()
                .scoringId(SCORING_ID)
                .requestId(REQUEST_ID)
                .partyId(PARTY_ID)
                .partyName(PARTY_NAME)
                .build();
        final AiReportContent aiContent = AiReportContent.builder()
                .title("Informe")
                .aiSummary("Resumen")
                .riskAnalysis("Análisis")
                .riskFactors(List.of(RiskFactor.builder()
                        .factor("DTI alto")
                        .severity(Severity.ALTO)
                        .description("Endeudamiento elevado")
                        .build()))
                .recommendations(List.of("Revisar ingresos"))
                .build();

        when(fetchScoringPort.fetchScoringData(REQUEST_ID)).thenReturn(scoringData);
        when(generateAiReportPort.generateReport(scoringData)).thenReturn(aiContent);
        when(generateAiReportPort.getModelName()).thenReturn(MODEL);
        when(renderReportPdfPort.render(any(Report.class)))
                .thenReturn(new RenderedPdf(new byte[] {1, 2, 3, 4}, 4, "/tmp/report.pdf"));

        final Report result = service.generateReport(REQUEST_ID);

        assertThat(result.getScoringId()).isEqualTo(SCORING_ID);
        assertThat(result.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(result.getPartyId()).isEqualTo(PARTY_ID);
        assertThat(result.getReportType()).isEqualTo(ReportType.RISK_ANALYSIS);
        assertThat(result.getTitle())
                .isEqualTo("Informe de Evaluación de Riesgo de Crédito - " + PARTY_NAME);
        assertThat(result.getGeneratedBy()).isEqualTo("ms-reporting");
        assertThat(result.getLanguage()).isEqualTo("es");
        assertThat(result.getModelVersion()).isEqualTo(MODEL);
        assertThat(result.getFileSizeBytes()).isEqualTo(4);
        assertThat(result.getFilePath()).isEqualTo("/tmp/report.pdf");
        assertThat(result.getGeneratedDate()).isNotNull();
        assertThat(result.getRiskFactors()).hasSize(1);

        final ArgumentCaptor<Report> stored = ArgumentCaptor.forClass(Report.class);
        verify(storeReportPort).store(stored.capture());
        assertThat(stored.getValue().getScoringId()).isEqualTo(SCORING_ID);
    }

    @Test
    void shouldFallBackToAiTitleWhenPartyNameMissing() {
        final ScoringData scoringData = ScoringData.builder()
                .scoringId(SCORING_ID)
                .requestId(REQUEST_ID)
                .partyId(PARTY_ID)
                .build();
        final AiReportContent aiContent = AiReportContent.builder()
                .title("Informe generado por IA")
                .aiSummary("Resumen")
                .riskAnalysis("Análisis")
                .riskFactors(List.of())
                .recommendations(List.of("Revisar"))
                .build();

        when(fetchScoringPort.fetchScoringData(REQUEST_ID)).thenReturn(scoringData);
        when(generateAiReportPort.generateReport(scoringData)).thenReturn(aiContent);
        when(generateAiReportPort.getModelName()).thenReturn(MODEL);
        when(renderReportPdfPort.render(any(Report.class)))
                .thenReturn(new RenderedPdf(new byte[] {1, 2}, 2, null));

        final Report result = service.generateReport(REQUEST_ID);

        assertThat(result.getTitle()).isEqualTo("Informe generado por IA");
    }

    @Test
    void shouldPropagateScoringNotAvailableAndSkipPersistence() {
        when(fetchScoringPort.fetchScoringData(REQUEST_ID))
                .thenThrow(new ScoringNotAvailableException("not ready"));

        assertThatThrownBy(() -> service.generateReport(REQUEST_ID))
                .isInstanceOf(ScoringNotAvailableException.class);

        verify(generateAiReportPort, never()).generateReport(any());
        verify(renderReportPdfPort, never()).render(any());
        verify(storeReportPort, never()).store(any());
    }

    @Test
    void shouldPropagateAiReportGenerationException() {
        final ScoringData scoringData = ScoringData.builder()
                .scoringId(SCORING_ID)
                .requestId(REQUEST_ID)
                .partyId(PARTY_ID)
                .build();

        when(fetchScoringPort.fetchScoringData(REQUEST_ID)).thenReturn(scoringData);
        when(generateAiReportPort.generateReport(scoringData))
                .thenThrow(new RuntimeException("Gemini API Timeout"));

        assertThatThrownBy(() -> service.generateReport(REQUEST_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Gemini API Timeout");

        verify(renderReportPdfPort, never()).render(any());
        verify(storeReportPort, never()).store(any());
    }
}
