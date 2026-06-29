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
 *
 * Maps to the "reports" MongoDB collection owned by ms-core-data. The
 * {@code filePath} is optional until the PDF binary storage target is decided;
 * {@code fileSizeBytes} always reflects the size of the rendered PDF.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    private String partyId;
    private String requestId;
    private String scoringId;
    private ReportType reportType;
    private String title;
    private String aiSummary;
    private String riskAnalysis;
    private List<RiskFactor> riskFactors;
    private List<String> recommendations;
    private String filePath;
    private int fileSizeBytes;
    private String generatedBy;
    private Date generatedDate;
    private int generationTimeMs;
    private String modelVersion;
    private String language;
}
