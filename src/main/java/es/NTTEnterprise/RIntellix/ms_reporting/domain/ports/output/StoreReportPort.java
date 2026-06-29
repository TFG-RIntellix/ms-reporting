package es.NTTEnterprise.RIntellix.ms_reporting.domain.ports.output;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;

/**
 * Output port to persist the report document through ms-core-data.
 */
public interface StoreReportPort {

    /**
     * Stores the report by calling ms-core-data.
     *
     * @param report the report to persist.
     */
    void store(Report report);
}
