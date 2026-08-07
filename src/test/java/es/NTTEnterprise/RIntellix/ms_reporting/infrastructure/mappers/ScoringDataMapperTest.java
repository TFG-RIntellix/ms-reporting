package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.CoreScoringResponseDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.CoreTopFeatureDTO;

class ScoringDataMapperTest {

    @Test
    @DisplayName("Should map CoreScoringResponseDTO to ScoringData successfully")
    void toDomain_success() {
        CoreScoringResponseDTO dto = new CoreScoringResponseDTO();
        dto.setScoringId("SCO-1");
        dto.setRequestId("REQ-1");
        dto.setModelVersion("v1.0");
        dto.setScoringDate("2026-07-17T12:00:00Z");
        dto.setInputFeatures(Map.of("age", 30));
        dto.setPd(0.05);
        dto.setLgd(0.40);
        dto.setEad(10000.0);
        dto.setEcl(200.0);
        dto.setRiskGrade("LOW");
        dto.setMonthlyPayment(500.0);
        dto.setDti(0.3);
        dto.setTotalPayment(12000.0);
        dto.setTotalInterest(2000.0);
        dto.setMonthlyDisposableIncome(1500.0);
        dto.setBaseValue(1.5);

        CoreTopFeatureDTO tf = new CoreTopFeatureDTO();
        tf.setFeatureName("age");
        tf.setFeatureValue("30");
        tf.setShapValue(0.1);
        dto.setTopFeatures(List.of(tf));

        ScoringData data = ScoringDataMapper.toDomain(dto, "P-1", "John Doe");

        assertEquals("SCO-1", data.getScoringId());
        assertEquals("REQ-1", data.getRequestId());
        assertEquals("P-1", data.getPartyId());
        assertEquals("John Doe", data.getPartyName());
        assertEquals("v1.0", data.getModelVersion());
        assertNotNull(data.getScoringDate());
        assertEquals(30, data.getInputFeatures().get("age"));
        assertEquals(0.05, data.getPd());
        assertEquals("LOW", data.getRiskGrade());
        assertEquals(1, data.getTopFeatures().size());
        assertEquals("age", data.getTopFeatures().get(0).getFeatureName());
        assertEquals(0.1, data.getTopFeatures().get(0).getShapValue());
    }

    @Test
    @DisplayName("Should map DTO with null collections safely")
    void toDomain_nullCollections() {
        CoreScoringResponseDTO dto = new CoreScoringResponseDTO();
        dto.setScoringId("SCO-2");

        ScoringData data = ScoringDataMapper.toDomain(dto, "P-2", "Jane");

        assertEquals("SCO-2", data.getScoringId());
        assertNotNull(data.getTopFeatures());
        assertTrue(data.getTopFeatures().isEmpty());
    }

    @Test
    @DisplayName("Should throw NPE when DTO is null")
    void toDomain_nullDTO() {
        assertThrows(NullPointerException.class, () -> {
            ScoringDataMapper.toDomain(null, "P-1", "Jane");
        });
    }
}
