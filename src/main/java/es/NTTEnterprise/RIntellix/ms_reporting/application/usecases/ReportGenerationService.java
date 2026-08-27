package es.NTTEnterprise.RIntellix.ms_reporting.application.usecases;

import java.util.Objects;

import es.NTTEnterprise.RIntellix.ms_reporting.application.ports.input.ReportGenerationPortService;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.AiReportContent;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RenderedPdf;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.factories.ReportFactory;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.FetchScoringPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.GenerateAiReportPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.RenderReportPdfPort;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.StoreReportPort;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Application service orchestrating the risk report generation pipeline:
 * - Fetches the authoritative scoring (and scoringId/partyId) from
 * ms-core-data, also acting as the race-condition guard.
 * - Generates the natural-language report through the AI analyst (Gemini).
 * - Renders the report to a PDF binary.
 * - Persists the report document through ms-core-data.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Slf4j
public class ReportGenerationService implements ReportGenerationPortService {

    private final FetchScoringPort fetchScoringPort;
    private final GenerateAiReportPort generateAiReportPort;
    private final RenderReportPdfPort renderReportPdfPort;
    private final StoreReportPort storeReportPort;

    /**
     * Constructs a ReportGenerationService with its output port dependencies.
     *
     * @param fetchScoringPort     the port to fetch scoring data
     * @param generateAiReportPort the port to generate AI reports
     * @param renderReportPdfPort  the port to render PDF documents
     * @param storeReportPort      the port to persist reports
     */
    public ReportGenerationService(
            final FetchScoringPort fetchScoringPort,
            final GenerateAiReportPort generateAiReportPort,
            final RenderReportPdfPort renderReportPdfPort,
            final StoreReportPort storeReportPort) {
        this.fetchScoringPort = Objects.requireNonNull(fetchScoringPort);
        this.generateAiReportPort = Objects.requireNonNull(generateAiReportPort);
        this.renderReportPdfPort = Objects.requireNonNull(renderReportPdfPort);
        this.storeReportPort = Objects.requireNonNull(storeReportPort);
    }

    @Override
    public Report generateReport(final String requestId) {
        Objects.requireNonNull(requestId, ReportConstants.REQUEST_ID_REQUIRED_MSG);
        log.info(LogMessage.REPORT_GENERATION_START, requestId);

        final long startNanos = System.nanoTime();

        // 1. Fetch scoring + identifiers (throws ScoringNotAvailableException to
        // trigger a Kafka retry when the scoring is not yet persisted).
        final ScoringData scoringData = fetchScoringPort.fetchScoringData(requestId);
        log.info(LogMessage.REPORT_SCORING_FETCHED, requestId,
                scoringData.getScoringId(), scoringData.getPartyId());

        // 2. Generate the AI report content.
        final AiReportContent aiContent = generateAiReportPort.generateReport(scoringData);
        log.info(LogMessage.REPORT_AI_GENERATED, requestId, generateAiReportPort.getModelName(),
                aiContent.getRiskFactors() != null ? aiContent.getRiskFactors().size() : 0);

        final Report report = ReportFactory.createReport(scoringData, aiContent, generateAiReportPort.getModelName());

        // 3. Render the PDF binary.
        final RenderedPdf pdf = renderReportPdfPort.render(report);
        report.setFileSizeBytes(pdf.sizeBytes());
        report.setFilePath(pdf.filePath());
        log.info(LogMessage.REPORT_PDF_RENDERED, requestId, pdf.sizeBytes(), pdf.filePath());

        // 4. Record timing and persist the report through ms-core-data.
        final int generationTimeMs = (int) ((System.nanoTime() - startNanos) / 1_000_000L);
        report.setGenerationTimeMs(generationTimeMs);

        storeReportPort.store(report);
        log.info(LogMessage.REPORT_PERSISTED, requestId, scoringData.getScoringId(), generationTimeMs);

        return report;
    }
}
