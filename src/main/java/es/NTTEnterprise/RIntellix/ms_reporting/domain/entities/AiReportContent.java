package es.NTTEnterprise.RIntellix.ms_reporting.domain.entities;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Natural-language content produced by the AI analyst (Gemini) for a scoring.
 * This is the structured output that feeds both the PDF and the persisted
 * report document.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReportContent {

    /**
     * The title of the generated report.
     */
    private String title;

    /**
     * A brief executive summary of the scoring.
     */
    private String aiSummary;

    /**
     * A detailed analysis of the credit risk.
     */
    private String riskAnalysis;

    /**
     * List of key risk factors identified.
     */
    private List<RiskFactor> riskFactors;

    /**
     * Actionable recommendations for the credit analyst.
     */
    private List<String> recommendations;
}
