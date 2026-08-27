package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.input.kafka;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import es.NTTEnterprise.RIntellix.ms_reporting.application.ports.input.ReportGenerationPortService;
import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.input.kafka.dtos.ScoringResultMessageDTO;

@ExtendWith(MockitoExtension.class)
class ScoringKafkaConsumerTest {

    @Mock
    private ReportGenerationPortService reportGenerationPortService;

    @Mock
    private Acknowledgment acknowledgment;

    private ScoringKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ScoringKafkaConsumer(reportGenerationPortService);
    }

    @Test
    void consumeScoring_ShouldGenerateReportAndAcknowledge_WhenMessageHasRequestId() {
        ScoringResultMessageDTO message = new ScoringResultMessageDTO();
        message.setRequestId("req-123");

        consumer.consumeScoring(message, acknowledgment);

        verify(reportGenerationPortService).generateReport("req-123");
        verify(acknowledgment).acknowledge();
    }

    @Test
    void consumeScoring_ShouldNotGenerateReportButAcknowledge_WhenMessageIsNull() {
        consumer.consumeScoring(null, acknowledgment);

        verify(reportGenerationPortService, never()).generateReport(anyString());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void consumeScoring_ShouldNotGenerateReportButAcknowledge_WhenRequestIdIsNull() {
        ScoringResultMessageDTO message = new ScoringResultMessageDTO();
        message.setRequestId(null);

        consumer.consumeScoring(message, acknowledgment);

        verify(reportGenerationPortService, never()).generateReport(anyString());
        verify(acknowledgment).acknowledge();
    }
}
