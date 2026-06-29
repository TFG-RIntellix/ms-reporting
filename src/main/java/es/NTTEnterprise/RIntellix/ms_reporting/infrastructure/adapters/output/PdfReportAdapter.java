package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RenderedPdf;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.PdfGenerationException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.RenderReportPdfPort;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that renders a {@link Report} into a PDF binary using OpenPDF.
 * The binary is always returned in-memory (so {@code file_size_bytes} can be
 * computed). When {@code report.pdf.output-dir} is configured the PDF is also
 * written to disk and its path is reported as {@code file_path}; otherwise
 * {@code file_path} stays {@code null} (it is optional).
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Slf4j
@Component
public class PdfReportAdapter implements RenderReportPdfPort {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADING_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font TABLE_HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font META_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);

    private final String outputDir;

    /**
     * Constructs a PdfReportAdapter with the output directory configuration value.
     *
     * @param outputDir the local directory path where PDFs will be stored
     */
    public PdfReportAdapter(@Value("${report.pdf.output-dir:}") final String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public RenderedPdf render(final Report report) {
        final byte[] content = buildPdf(report);
        final String filePath = writeToDisk(report, content);
        return new RenderedPdf(content, content.length, filePath);
    }

    /**
     * Constructs the PDF document binary.
     *
     * @param report the report entity containing textual details
     * @return the byte array of the rendered PDF
     */
    private byte[] buildPdf(final Report report) {
        final Document document = new Document(PageSize.A4);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document, report);
            addMetadata(document, report);
            addSection(document, ReportConstants.PDF_SECTION_SUMMARY, report.getAiSummary());
            addSection(document, ReportConstants.PDF_SECTION_ANALYSIS, report.getRiskAnalysis());
            addRiskFactors(document, report.getRiskFactors());
            addRecommendations(document, report.getRecommendations());

            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            if (document.isOpen()) {
                document.close();
            }
            throw new PdfGenerationException(LogMessage.PDF_RENDER_ERROR, ex);
        }
    }

    /**
     * Adds the main report title to the PDF page.
     *
     * @param document the PDF Document target
     * @param report   the report details
     */
    private void addTitle(final Document document, final Report report) {
        final String title = StringUtils.hasText(report.getTitle())
                ? report.getTitle() : ReportConstants.PDF_DEFAULT_TITLE;
        final Paragraph titleParagraph = new Paragraph(title, TITLE_FONT);
        titleParagraph.setSpacingAfter(8f);
        document.add(titleParagraph);
    }

    /**
     * Adds metadata section at the top of the report.
     *
     * @param document the PDF Document target
     * @param report   the report details
     */
    private void addMetadata(final Document document, final Report report) {
        final StringBuilder meta = new StringBuilder();
        meta.append(ReportConstants.PDF_META_REQUEST_PREFIX).append(nullSafe(report.getRequestId()));
        meta.append(ReportConstants.PDF_META_SCORING_SEPARATOR).append(nullSafe(report.getScoringId()));
        meta.append(ReportConstants.PDF_META_MODEL_SEPARATOR).append(nullSafe(report.getModelVersion()));
        final Paragraph metaParagraph = new Paragraph(meta.toString(), META_FONT);
        metaParagraph.setSpacingAfter(14f);
        document.add(metaParagraph);
    }

    /**
     * Adds a section with a heading and a textual paragraph body.
     *
     * @param document the PDF Document target
     * @param heading  the section title
     * @param body     the content string
     */
    private void addSection(final Document document, final String heading, final String body) {
        final Paragraph headingParagraph = new Paragraph(heading, HEADING_FONT);
        headingParagraph.setSpacingBefore(8f);
        headingParagraph.setSpacingAfter(4f);
        document.add(headingParagraph);

        final Paragraph bodyParagraph = new Paragraph(StringUtils.hasText(body) ? body : ReportConstants.PDF_HYPHEN, BODY_FONT);
        bodyParagraph.setSpacingAfter(10f);
        document.add(bodyParagraph);
    }

    /**
     * Builds and adds the risk factors table.
     *
     * @param document    the PDF Document target
     * @param riskFactors list of risk factors to include
     */
    private void addRiskFactors(final Document document, final List<RiskFactor> riskFactors) {
        final Paragraph heading = new Paragraph(ReportConstants.PDF_SECTION_FACTORS, HEADING_FONT);
        heading.setSpacingBefore(8f);
        heading.setSpacingAfter(4f);
        document.add(heading);

        if (riskFactors == null || riskFactors.isEmpty()) {
            document.add(new Paragraph(ReportConstants.PDF_HYPHEN, BODY_FONT));
            return;
        }

        final PdfPTable table = new PdfPTable(new float[] {3f, 1.4f, 5f});
        table.setWidthPercentage(100f);
        table.setSpacingAfter(10f);
        addHeaderCell(table, ReportConstants.PDF_TABLE_HEADER_FACTOR);
        addHeaderCell(table, ReportConstants.PDF_TABLE_HEADER_SEVERITY);
        addHeaderCell(table, ReportConstants.PDF_TABLE_HEADER_DESCRIPTION);

        for (final RiskFactor factor : riskFactors) {
            table.addCell(new Phrase(nullSafe(factor.getFactor()), BODY_FONT));
            table.addCell(new Phrase(factor.getSeverity() != null ? factor.getSeverity().name() : ReportConstants.PDF_HYPHEN, BODY_FONT));
            table.addCell(new Phrase(nullSafe(factor.getDescription()), BODY_FONT));
        }
        document.add(table);
    }

    /**
     * Builds and adds the list of recommendations.
     *
     * @param document        the PDF Document target
     * @param recommendations list of recommendation items
     */
    private void addRecommendations(final Document document, final List<String> recommendations) {
        final Paragraph heading = new Paragraph(ReportConstants.PDF_SECTION_RECOMMENDATIONS, HEADING_FONT);
        heading.setSpacingBefore(8f);
        heading.setSpacingAfter(4f);
        document.add(heading);

        if (recommendations == null || recommendations.isEmpty()) {
            document.add(new Paragraph(ReportConstants.PDF_HYPHEN, BODY_FONT));
            return;
        }
        for (final String recommendation : recommendations) {
            final Paragraph item = new Paragraph(ReportConstants.PDF_BULLET_PREFIX + nullSafe(recommendation), BODY_FONT);
            item.setSpacingAfter(2f);
            document.add(item);
        }
    }

    /**
     * Helper to append a single left-aligned header cell.
     *
     * @param table the table object target
     * @param text  the header column label
     */
    private void addHeaderCell(final PdfPTable table, final String text) {
        final PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    /**
     * Writes the PDF byte content locally to the filesystem if outputDir is present.
     *
     * @param report  the report metadata to structure file naming
     * @param content the binary PDF byte array
     * @return the resolved file absolute path, or null
     */
    private String writeToDisk(final Report report, final byte[] content) {
        if (!StringUtils.hasText(outputDir)) {
            return null;
        }
        try {
            final Path directory = Path.of(outputDir);
            Files.createDirectories(directory);
            final String fileName = String.format(ReportConstants.PDF_FILE_NAME_PATTERN,
                    nullSafe(report.getRequestId()), nullSafe(report.getScoringId()));
            final Path target = directory.resolve(fileName);
            Files.write(target, content);
            return target.toAbsolutePath().toString();
        } catch (Exception ex) {
            log.warn(LogMessage.PDF_WRITE_WARN, outputDir, ex);
            return null;
        }
    }

    /**
     * Resolves value safely to hyphen if null.
     *
     * @param value raw value
     * @return non-null formatted string
     */
    private String nullSafe(final String value) {
        return value != null ? value : ReportConstants.PDF_HYPHEN;
    }
}
