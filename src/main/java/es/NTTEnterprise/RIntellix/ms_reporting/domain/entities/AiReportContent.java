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
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReportContent {

    private String title;
    private String aiSummary;
    private String riskAnalysis;
    private List<RiskFactor> riskFactors;
    private List<String> recommendations;
}
