package es.NTTEnterprise.RIntellix.ms_reporting.domain.entities;

import java.util.Date;
import java.util.List;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain aggregate representing a generated risk report.
 * Maps to the "reports" MongoDB collection owned by ms-core-data.
 * The filePath is optional until the PDF binary storage target is decided;
 * fileSizeBytes always reflects the size of the rendered PDF.
 * 
 * @author Lucía Fernández Mancebo
 * @Date 29-06-2026
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    /**
     * Unique identifier of the associated party.
     */
    private String partyId;

    /**
     * Unique identifier of the credit request.
     */
    private String requestId;

    /**
     * Unique identifier of the computed scoring.
     */
    private String scoringId;

    /**
     * Type of report generated.
     */
    private ReportType reportType;

    /**
     * The title of the generated report.
     */
    private String title;

    /**
     * A brief AI generated summary.
     */
    private String aiSummary;

    /**
     * Detailed credit risk analysis.
     */
    private String riskAnalysis;

    /**
     * List of identified risk factors.
     */
    private List<RiskFactor> riskFactors;

    /**
     * List of actionable recommendations.
     */
    private List<String> recommendations;

    /**
     * The path to the stored PDF file on disk.
     */
    private String filePath;

    /**
     * Size of the generated PDF report in bytes.
     */
    private int fileSizeBytes;

    /**
     * Service or agent that generated this report.
     */
    private String generatedBy;

    /**
     * Timestamp when the report was generated.
     */
    private Date generatedDate;

    /**
     * Time taken to generate the report in milliseconds.
     */
    private int generationTimeMs;

    /**
     * Version of the AI model used to generate the report content.
     */
    private String modelVersion;

    /**
     * Language of the generated report.
     */
    private String language;
}
