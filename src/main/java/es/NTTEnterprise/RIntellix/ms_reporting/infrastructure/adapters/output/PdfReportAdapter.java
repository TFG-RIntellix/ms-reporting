package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RenderedPdf;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.PdfGenerationException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.RenderReportPdfPort;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.view.PdfViewModel;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers.PdfViewModelMapper;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.ReportConstants;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Output adapter that renders a {@link Report} into a PDF binary using
 * Thymeleaf and Playwright.
 * The binary is always returned in-memory. When configured, it writes the file
 * to disk.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Slf4j
@Component
public class PdfReportAdapter implements RenderReportPdfPort {

    private final String outputDir;
    private final TemplateEngine templateEngine;
    private final PdfViewModelMapper pdfViewModelMapper;

    @PostConstruct
    public void initPlaywright() {
        log.info("Inicializando motor Playwright y asegurando binarios Chromium en caché...");
        try (Playwright playwright = Playwright.create()) {
            log.info(LogMessage.PLAYWRIGHT_INIT_SUCCESS);
        } catch (Exception ex) {
            log.error(LogMessage.PLAYWRIGHT_INIT_ERROR, ex);
        }
    }

    /**
     * Constructs a PdfReportAdapter with the output directory configuration,
     * template engine and view model mapper.
     *
     * @param outputDir          the local directory path where PDFs will be stored
     * @param templateEngine     the Thymeleaf template engine
     * @param pdfViewModelMapper the mapper to construct the view model
     */
    public PdfReportAdapter(@Value("${report.pdf.output-dir:}") final String outputDir,
            final TemplateEngine templateEngine,
            final PdfViewModelMapper pdfViewModelMapper) {
        this.outputDir = outputDir;
        this.templateEngine = templateEngine;
        this.pdfViewModelMapper = pdfViewModelMapper;
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

        final PdfViewModel dto = pdfViewModelMapper.toViewModel(report);
        context.setVariable("dto", dto);
        
        return templateEngine.process("credit_report", context);
    }

    /**
     * Converts the HTML string into a PDF byte array using Playwright.
     */
    protected byte[] generatePdf(final String html) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            page.setContent(html);
            byte[] pdfBytes = page.pdf(new Page.PdfOptions().setFormat("A4").setPrintBackground(true));
            browser.close();
            return pdfBytes;
        } catch (Exception ex) {
            throw new PdfGenerationException(LogMessage.PDF_RENDER_ERROR, ex);
        }
    }

    /**
     * Writes the PDF byte content locally to the filesystem if outputDir is
     * present.
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
