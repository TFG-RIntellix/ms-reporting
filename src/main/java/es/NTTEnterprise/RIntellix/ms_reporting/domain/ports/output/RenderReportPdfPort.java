package es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.RenderedPdf;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;

/**
 * Output port to render a report into a PDF binary.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
public interface RenderReportPdfPort {

    /**
     * Renders the given report to a PDF.
     *
     * @param report the report to render
     * @return the rendered PDF bytes, its size and optional file path
     */
    RenderedPdf render(Report report);
}
