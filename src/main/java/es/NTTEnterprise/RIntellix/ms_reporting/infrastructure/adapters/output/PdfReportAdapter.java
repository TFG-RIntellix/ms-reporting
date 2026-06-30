package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RenderedPdf;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.PdfGenerationException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.RenderReportPdfPort;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that renders a {@link Report} into a PDF binary using Thymeleaf and OpenHtmlToPdf.
 * The binary is always returned in-memory. When configured, it writes the file to disk.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Slf4j
@Component
public class PdfReportAdapter implements RenderReportPdfPort {

    private final String outputDir;
    private final TemplateEngine templateEngine;

    /**
     * Constructs a PdfReportAdapter with the output directory configuration and template engine.
     *
     * @param outputDir      the local directory path where PDFs will be stored
     * @param templateEngine the Thymeleaf template engine
     */
    public PdfReportAdapter(@Value("${report.pdf.output-dir:}") final String outputDir,
                            final TemplateEngine templateEngine) {
        this.outputDir = outputDir;
        this.templateEngine = templateEngine;
    }

    @Override
    public RenderedPdf render(final Report report) {
        final String htmlContent = generateHtml(report);
        final byte[] pdfContent = generatePdf(htmlContent);
        final String filePath = writeToDisk(report, pdfContent);
        return new RenderedPdf(pdfContent, pdfContent.length, filePath);
    }

    /**
     * Converts the report into an HTML string using Thymeleaf.
     */
    private String generateHtml(final Report report) {
        final Context context = new Context();
        context.setVariable("report", report);
        
        // Calculate a mock Grade based on worst severity for the UI
        String grade = "A";
        if (report.getRiskFactors() != null) {
            for (RiskFactor factor : report.getRiskFactors()) {
                if (factor.getSeverity() != null && factor.getSeverity().name().equals("ALTO")) {
                    grade = "D";
                    break;
                }
            }
        }
        
        Map<String, Object> dto = new HashMap<>();
        dto.put("grade", grade);
        context.setVariable("dto", dto);

        return templateEngine.process("credit_report", context);
    }

    /**
     * Converts the HTML string into a PDF byte array using OpenHtmlToPdf.
     */
    private byte[] generatePdf(final String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, "");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception ex) {
            throw new PdfGenerationException(LogMessage.PDF_RENDER_ERROR, ex);
        }
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
     */
    private String nullSafe(final String value) {
        return value != null ? value : ReportConstants.PDF_HYPHEN;
    }
}
