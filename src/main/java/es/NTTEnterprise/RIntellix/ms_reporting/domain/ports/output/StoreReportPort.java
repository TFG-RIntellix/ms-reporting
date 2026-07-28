package es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;

/**
 * Output port to persist the report document through ms-core-data.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
public interface StoreReportPort {

    /**
     * Stores the report by calling ms-core-data.
     *
     * @param report the report to persist
     */
    void store(Report report);
}
