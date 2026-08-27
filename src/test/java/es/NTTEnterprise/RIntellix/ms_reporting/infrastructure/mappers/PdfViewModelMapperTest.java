package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.Report;
import es.NTTEnterprise.RIntellix.ms_reporting.domain.entities.ScoringData;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.output.dtos.view.PdfViewModel;

class PdfViewModelMapperTest {

    private PdfViewModelMapper pdfViewModelMapper;

    @BeforeEach
    void setUp() {
        pdfViewModelMapper = new PdfViewModelMapper();
    }

    @Test
    void shouldMapEmptyScoringDataToDefaultViewModel() {
        Report report = new Report();
        PdfViewModel viewModel = pdfViewModelMapper.toViewModel(report);

        assertNotNull(viewModel);
        assertEquals("N/A", viewModel.grade());
        assertEquals("N/A", viewModel.decision());
        assertEquals("Desconocido", viewModel.clientName());
        assertTrue(viewModel.kpis().isEmpty());
    }

    @Test
    void shouldMapScoringDataToViewModel() {
        ScoringData data = new ScoringData();
        data.setRiskGrade("C");
        data.setPartyName("Juan Perez");
        data.setPd(0.05);
        data.setLgd(0.45);
        data.setEad(150000.0);
        data.setEcl(3375.0);
        data.setDti(0.35);

        data.setInputFeatures(Map.of(
                "loanType", "HIPOTECA",
                "purpose", "COMPRA_VIVIENDA",
                "annualIncome", 60000.0,
                "loanAmount", 150000.0,
                "interestRate", 0.035,
                "termMonths", 360,
                "ltv", 0.80));

        Report report = new Report();
        report.setScoringData(data);

        PdfViewModel viewModel = pdfViewModelMapper.toViewModel(report);

        assertNotNull(viewModel);
        assertEquals("C", viewModel.grade());
        assertEquals("APROBAR", viewModel.decision());
        assertEquals("Juan Perez", viewModel.clientName());
        assertEquals("HIPOTECA — COMPRA VIVIENDA", viewModel.loanType());
        
        List<Map<String, String>> kpis = viewModel.kpis();
        assertEquals(4, kpis.size());
        assertEquals("Probabilidad de Impago (PD)", kpis.get(0).get("label"));
        assertEquals("5,00%", kpis.get(0).get("value"));
        
        List<Map<String, String>> financials = viewModel.financials();
        assertEquals(8, financials.size());
    }
}
