package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.output;

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
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that renders a {@link Report} into a PDF binary using OpenPDF.
 *
 * The binary is always returned in-memory (so {@code file_size_bytes} can be
 * computed). When {@code report.pdf.output-dir} is configured the PDF is also
 * written to disk and its path is reported as {@code file_path}; otherwise
 * {@code file_path} stays {@code null} (it is optional).
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

    public PdfReportAdapter(@Value("${report.pdf.output-dir:}") final String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    public RenderedPdf render(final Report report) {
        final byte[] content = buildPdf(report);
        final String filePath = writeToDisk(report, content);
        return new RenderedPdf(content, content.length, filePath);
    }

    private byte[] buildPdf(final Report report) {
        final Document document = new Document(PageSize.A4);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, baos);
            document.open();

            addTitle(document, report);
            addMetadata(document, report);
            addSection(document, "Resumen ejecutivo", report.getAiSummary());
            addSection(document, "Análisis de riesgo", report.getRiskAnalysis());
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

    private void addTitle(final Document document, final Report report) {
        final String title = StringUtils.hasText(report.getTitle())
                ? report.getTitle() : "Informe de riesgo de crédito";
        final Paragraph titleParagraph = new Paragraph(title, TITLE_FONT);
        titleParagraph.setSpacingAfter(8f);
        document.add(titleParagraph);
    }

    private void addMetadata(final Document document, final Report report) {
        final StringBuilder meta = new StringBuilder();
        meta.append("Request: ").append(nullSafe(report.getRequestId()));
        meta.append("  |  Scoring: ").append(nullSafe(report.getScoringId()));
        meta.append("  |  Modelo IA: ").append(nullSafe(report.getModelVersion()));
        final Paragraph metaParagraph = new Paragraph(meta.toString(), META_FONT);
        metaParagraph.setSpacingAfter(14f);
        document.add(metaParagraph);
    }

    private void addSection(final Document document, final String heading, final String body) {
        final Paragraph headingParagraph = new Paragraph(heading, HEADING_FONT);
        headingParagraph.setSpacingBefore(8f);
        headingParagraph.setSpacingAfter(4f);
        document.add(headingParagraph);

        final Paragraph bodyParagraph = new Paragraph(StringUtils.hasText(body) ? body : "-", BODY_FONT);
        bodyParagraph.setSpacingAfter(10f);
        document.add(bodyParagraph);
    }

    private void addRiskFactors(final Document document, final List<RiskFactor> riskFactors) {
        final Paragraph heading = new Paragraph("Factores de riesgo", HEADING_FONT);
        heading.setSpacingBefore(8f);
        heading.setSpacingAfter(4f);
        document.add(heading);

        if (riskFactors == null || riskFactors.isEmpty()) {
            document.add(new Paragraph("-", BODY_FONT));
            return;
        }

        final PdfPTable table = new PdfPTable(new float[] {3f, 1.4f, 5f});
        table.setWidthPercentage(100f);
        table.setSpacingAfter(10f);
        addHeaderCell(table, "Factor");
        addHeaderCell(table, "Severidad");
        addHeaderCell(table, "Descripción");

        for (final RiskFactor factor : riskFactors) {
            table.addCell(new Phrase(nullSafe(factor.getFactor()), BODY_FONT));
            table.addCell(new Phrase(factor.getSeverity() != null ? factor.getSeverity().name() : "-", BODY_FONT));
            table.addCell(new Phrase(nullSafe(factor.getDescription()), BODY_FONT));
        }
        document.add(table);
    }

    private void addRecommendations(final Document document, final List<String> recommendations) {
        final Paragraph heading = new Paragraph("Recomendaciones", HEADING_FONT);
        heading.setSpacingBefore(8f);
        heading.setSpacingAfter(4f);
        document.add(heading);

        if (recommendations == null || recommendations.isEmpty()) {
            document.add(new Paragraph("-", BODY_FONT));
            return;
        }
        for (final String recommendation : recommendations) {
            final Paragraph item = new Paragraph("• " + nullSafe(recommendation), BODY_FONT);
            item.setSpacingAfter(2f);
            document.add(item);
        }
    }

    private void addHeaderCell(final PdfPTable table, final String text) {
        final PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    private String writeToDisk(final Report report, final byte[] content) {
        if (!StringUtils.hasText(outputDir)) {
            return null;
        }
        try {
            final Path directory = Path.of(outputDir);
            Files.createDirectories(directory);
            final String fileName = String.format("report_%s_%s.pdf",
                    nullSafe(report.getRequestId()), nullSafe(report.getScoringId()));
            final Path target = directory.resolve(fileName);
            Files.write(target, content);
            return target.toAbsolutePath().toString();
        } catch (Exception ex) {
            log.warn(LogMessage.PDF_WRITE_WARN, outputDir, ex);
            return null;
        }
    }

    private String nullSafe(final String value) {
        return value != null ? value : "-";
    }
}
