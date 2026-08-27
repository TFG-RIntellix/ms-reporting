package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.view;

import java.util.List;
import java.util.Map;

/**
 * View Model containing the formatted data required by the PDF template.
 * This class isolates presentation concerns (formatting, strings) from the Domain layer.
 * 
 * @author Lucía Fernández Mancebo
 * @date 20/08/2026
 */
public record PdfViewModel(
        String grade,
        String decision,
        String loanType,
        String clientName,
        List<Map<String, String>> kpis,
        List<Map<String, String>> financials,
        String dti,
        String dtiDasharray,
        String dtiText,
        List<Map<String, Object>> shapFactors) {
}
