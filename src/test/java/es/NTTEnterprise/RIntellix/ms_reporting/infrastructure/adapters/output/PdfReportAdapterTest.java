package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import org.mockito.Mockito;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RenderedPdf;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.ReportType;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.Severity;

class PdfReportAdapterTest {

    @Test
    void shouldRenderNonEmptyPdfWithoutFilePathWhenOutputDirNotConfigured() {
        final TemplateEngine templateEngine = Mockito.mock(TemplateEngine.class);
        Mockito.when(templateEngine.process(
                Mockito.eq("credit_report"),
                Mockito.any(Context.class)))
            .thenReturn("<html><body><h1>Informe de Calificacion</h1></body></html>");

        final PdfReportAdapter adapter = spy(new PdfReportAdapter("", templateEngine));
        byte[] fakePdfBytes = "%PDF-1.4 fake".getBytes(StandardCharsets.ISO_8859_1);
        doReturn(fakePdfBytes).when(adapter).generatePdf(Mockito.anyString());
        final Report report = sampleReport();

        final RenderedPdf rendered = adapter.render(report);

        assertThat(rendered.content()).isNotEmpty();
        assertThat(rendered.sizeBytes()).isEqualTo(rendered.content().length);
        assertThat(rendered.filePath()).isNull();

        final String header = new String(rendered.content(), 0, 5, StandardCharsets.ISO_8859_1);
        assertThat(header).isEqualTo("%PDF-");
    }

    private Report sampleReport() {
        return Report.builder()
                .partyId("party-1")
                .requestId("req-1")
                .scoringId("sco-1")
                .reportType(ReportType.RISK_ANALYSIS)
                .title("Informe de riesgo de crédito")
                .aiSummary("Resumen ejecutivo de prueba")
                .riskAnalysis("Análisis de riesgo de prueba")
                .riskFactors(List.of(RiskFactor.builder()
                        .factor("DTI elevado")
                        .severity(Severity.ALTO)
                        .description("Ratio de endeudamiento alto")
                        .build()))
                .recommendations(List.of("Solicitar avalista", "Reducir importe"))
                .generatedBy("ms-reporting")
                .generatedDate(new Date())
                .modelVersion("gemini-2.0-flash")
                .language("es")
                .build();
    }
}
