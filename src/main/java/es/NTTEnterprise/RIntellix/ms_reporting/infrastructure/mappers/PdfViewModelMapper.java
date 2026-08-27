package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.TopFeature;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.view.PdfViewModel;

/**
 * Mapper that transforms a {@link Report} domain entity into a {@link PdfViewModel}.
 * Handles all UI formatting, percentages, and logic related to presentation.
 * 
 * @author Lucía Fernández Mancebo
 * @date 20/08/2026
 */
@Component
public class PdfViewModelMapper {

    /**
     * Maps the Report domain object to a PdfViewModel for the HTML template.
     *
     * @param report the generated report.
     * @return the structured view model.
     */
    public PdfViewModel toViewModel(final Report report) {
        final ScoringData data = report.getScoringData();

        if (data == null) {
            return new PdfViewModel(
                    "N/A", "N/A", "N/A", "Desconocido",
                    List.of(), List.of(), "0%", "0",
                    "Sin datos disponibles.", List.of());
        }

        final String grade = data.getRiskGrade() != null ? data.getRiskGrade() : "A";
        final boolean isRisk = "D".equals(grade) || "E".equals(grade) || "F".equals(grade) || "G".equals(grade);
        final String decision = isRisk ? "REVISAR" : "APROBAR";
        final String clientName = data.getPartyName() != null ? data.getPartyName() : "Desconocido";

        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

        String loanType = "-";
        if (data.getInputFeatures() != null && data.getInputFeatures().containsKey("loanType")
                && data.getInputFeatures().containsKey("purpose")) {
            loanType = data.getInputFeatures().get("loanType") + " — " + data.getInputFeatures().get("purpose");
        }
        loanType = loanType.replace("_", " ");

        final List<Map<String, String>> kpis = List.of(
                Map.of("label", "Probabilidad de Impago (PD)", "value", formatPercent(data.getPd())),
                Map.of("label", "Severidad de Pérdida (LGD)", "value", formatPercent(data.getLgd())),
                Map.of("label", "Exposición en Caso de Impago (EAD)", "value",
                        currencyFormat.format(safeDouble(data.getEad()))),
                Map.of("label", "Pérdida Esperada (ECL)", "value",
                        currencyFormat.format(safeDouble(data.getEcl()))));

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

        final List<Map<String, String>> financials = List.of(
                Map.of("label", "Ingreso Anual", "value", currencyFormat.format(safeDouble(annualIncome))),
                Map.of("label", "Monto Solicitado", "value", currencyFormat.format(safeDouble(loanAmount))),
                Map.of("label", "Cuota Mensual", "value",
                        currencyFormat.format(safeDouble(data.getMonthlyPayment()))),
                Map.of("label", "Ingreso Disponible", "value",
                        currencyFormat.format(safeDouble(data.getMonthlyDisposableIncome()))),
                Map.of("label", "Tasa de Interés", "value", formatPercent(interestRate)),
                Map.of("label", "Plazo", "value", termMonths + " meses"),
                Map.of("label", "LTV", "value", ltv > 0 ? formatPercent(ltv) : "N/A"),
                Map.of("label", "DTI", "value", formatPercent(data.getDti())));

        final Double dtiVal = safeDouble(data.getDti());
        final String dti = formatPercent(dtiVal);
        final String dtiDasharray = String.valueOf(Math.min(1.0, dtiVal) * 314.0);

        String dtiText;
        if (dtiVal > 0.40) {
            dtiText = "DTI elevado. Más del 40% de los ingresos mensuales se destina al servicio de deudas, reduciendo la resiliencia financiera.";
        } else {
            dtiText = "DTI saludable. El solicitante dedica una proporción manejable de sus ingresos al servicio de la deuda.";
        }

        final List<Map<String, Object>> shapFactors = new ArrayList<>();
        if (data.getTopFeatures() != null) {
            for (TopFeature tf : data.getTopFeatures()) {
                Double shap = tf.getShapValue() != null ? tf.getShapValue() : 0.0;
                String direction = shap > 0 ? "increase" : "reduce";
                String shapStr = (shap > 0 ? "+" : "") + String.format(Locale.US, "%.3f", shap);

                int widthPct = Math.min(100, Math.max(10, (int) (Math.abs(shap) * 100)));

                shapFactors.add(Map.of(
                        "name", tf.getFeatureName(),
                        "shap", shapStr,
                        "direction", direction,
                        "widthPct", widthPct));
            }
        }

        return new PdfViewModel(
                grade, decision, loanType, clientName,
                kpis, financials, dti, dtiDasharray, dtiText, shapFactors);
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
}
