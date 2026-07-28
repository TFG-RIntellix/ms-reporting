package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RiskFactor;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.TopFeature;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.exceptions.PdfGenerationException;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output.RenderReportPdfPort;
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
 * @Date 29-06-2026
 */
@Slf4j
@Component
public class PdfReportAdapter implements RenderReportPdfPort {

    private final String outputDir;
    private final TemplateEngine templateEngine;

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
     * Constructs a PdfReportAdapter with the output directory configuration and
     * template engine.
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

        Map<String, Object> dto = new HashMap<>();
        ScoringData data = report.getScoringData();

        if (data != null) {
            String grade = data.getRiskGrade() != null ? data.getRiskGrade() : "A";
            dto.put("grade", grade);

            boolean isRisk = "D".equals(grade) || "E".equals(grade) || "F".equals(grade) || "G".equals(grade);
            dto.put("decision", isRisk ? "REVISAR" : "APROBAR");

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

            String loanType = "-";
            if (data.getInputFeatures() != null && data.getInputFeatures().containsKey("loanType")
                    && data.getInputFeatures().containsKey("purpose")) {
                loanType = data.getInputFeatures().get("loanType") + " — " + data.getInputFeatures().get("purpose");
            }
            dto.put("loanType", loanType.replace("_", " "));

            dto.put("kpis", List.of(
                    Map.of("label", "Probabilidad de Impago (PD)", "value", formatPercent(data.getPd())),
                    Map.of("label", "Severidad de Pérdida (LGD)", "value", formatPercent(data.getLgd())),
                    Map.of("label", "Exposición en Caso de Impago (EAD)", "value",
                            currencyFormat.format(safeDouble(data.getEad()))),
                    Map.of("label", "Pérdida Esperada (ECL)", "value",
                            currencyFormat.format(safeDouble(data.getEcl())))));

            Double annualIncome = 0.0;
            Double loanAmount = 0.0;
            Double interestRate = 0.0;
            Integer termMonths = 0;
            Double ltv = 0.0;

            if (data.getInputFeatures() != null) {
                annualIncome = parseDoubleStrict(data.getInputFeatures().get("annual_income"));
                loanAmount = parseDoubleStrict(data.getInputFeatures().get("requested_amount") != null
                        ? data.getInputFeatures().get("requested_amount")
                        : data.getInputFeatures().get("requested_limit"));
                interestRate = parseDoubleStrict(data.getInputFeatures().get("interest_rate"));
                Object termObj = data.getInputFeatures().get("term_months");
                termMonths = termObj instanceof Number ? ((Number) termObj).intValue() : 0;
                ltv = parseDoubleStrict(data.getInputFeatures().get("ltv"));
            }

            dto.put("financials", List.of(
                    Map.of("label", "Ingreso Anual", "value", currencyFormat.format(safeDouble(annualIncome))),
                    Map.of("label", "Monto Solicitado", "value", currencyFormat.format(safeDouble(loanAmount))),
                    Map.of("label", "Cuota Mensual", "value",
                            currencyFormat.format(safeDouble(data.getMonthlyPayment()))),
                    Map.of("label", "Ingreso Disponible", "value",
                            currencyFormat.format(safeDouble(data.getMonthlyDisposableIncome()))),
                    Map.of("label", "Tasa de Interés", "value", formatPercent(interestRate)),
                    Map.of("label", "Plazo", "value", termMonths + " meses"),
                    Map.of("label", "LTV", "value", ltv > 0 ? formatPercent(ltv) : "N/A"),
                    Map.of("label", "DTI", "value", formatPercent(data.getDti()))));

            Double dtiVal = safeDouble(data.getDti());
            dto.put("dti", formatPercent(dtiVal));
            dto.put("dtiDasharray", String.valueOf(Math.min(1.0, dtiVal) * 314.0));

            if (dtiVal > 0.40) {
                dto.put("dtiText",
                        "DTI elevado. Más del 40% de los ingresos mensuales se destina al servicio de deudas, reduciendo la resiliencia financiera.");
            } else {
                dto.put("dtiText",
                        "DTI saludable. El solicitante dedica una proporción manejable de sus ingresos al servicio de la deuda.");
            }

            if (data.getTopFeatures() != null) {
                List<Map<String, Object>> shapList = new ArrayList<>();
                for (TopFeature tf : data.getTopFeatures()) {
                    Double shap = tf.getShapValue() != null ? tf.getShapValue() : 0.0;
                    String direction = shap > 0 ? "increase" : "reduce";
                    String shapStr = (shap > 0 ? "+" : "") + String.format(Locale.US, "%.3f", shap);

                    int widthPct = Math.min(100, Math.max(10, (int) (Math.abs(shap) * 100)));

                    shapList.add(Map.of(
                            "name", tf.getFeatureName(),
                            "shap", shapStr,
                            "direction", direction,
                            "widthPct", widthPct));
                }
                dto.put("shapFactors", shapList);
            } else {
                dto.put("shapFactors", List.of());
            }
        } else {
            dto.put("grade", "N/A");
            dto.put("decision", "N/A");
            dto.put("loanType", "N/A");
            dto.put("kpis", List.of());
            dto.put("financials", List.of());
            dto.put("dti", "0%");
            dto.put("dtiDasharray", "0");
            dto.put("dtiText", "Sin datos disponibles.");
            dto.put("shapFactors", List.of());
        }

        context.setVariable("dto", dto);
        return templateEngine.process("credit_report", context);
    }

    private String formatPercent(Double value) {
        if (value == null)
            return "0.00%";
        return String.format(new Locale("es", "ES"), "%.2f%%", value * 100);
    }

    private Double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private Double parseDoubleStrict(Object value) {
        if (value == null)
            return 0.0;
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
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
