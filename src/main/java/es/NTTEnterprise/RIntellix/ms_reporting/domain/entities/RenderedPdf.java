package es.NTTEnterprise.RIntellix.ms_reporting.domain.entities;

/**
 * Result of rendering a report to a PDF binary.
 * 
 * @param content   the raw PDF bytes
 * @param sizeBytes the size of the PDF in bytes
 * @param filePath  the location the PDF was written to, or {@code null} when no
 *                  storage target is configured
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
public record RenderedPdf(byte[] content, int sizeBytes, String filePath) {
}
