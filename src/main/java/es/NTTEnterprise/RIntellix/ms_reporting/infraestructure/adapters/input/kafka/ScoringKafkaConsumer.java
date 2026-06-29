package es.NTTEnterprise.RIntellix.ms_reporting.infraestructure.adapters.input.kafka;

import java.util.Objects;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import es.NTTEnterprise.RIntellix.ms_reporting.application.dtos.input.ScoringResultMessageDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.application.ports.input.ReportGenerationPortService;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer adapter for the persistScoring topic.
 *
 * Consumes the scoring result published by ms-risk-engine (the same message
 * ms-core-data persists) and triggers report generation. Uses manual
 * acknowledgment so the message is only committed after a report is generated
 * and persisted; failures are retried by the configured error handler.
 */
@Slf4j
@Component
@Validated
public class ScoringKafkaConsumer {

    private final ReportGenerationPortService reportGenerationPortService;

    public ScoringKafkaConsumer(final ReportGenerationPortService reportGenerationPortService) {
        this.reportGenerationPortService = Objects.requireNonNull(reportGenerationPortService);
    }

    @KafkaListener(topics = "${scoring.kafka.topic.persist}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeScoring(
            @Payload @Valid final ScoringResultMessageDTO message,
            final Acknowledgment acknowledgment) {

        final String requestId = message != null ? message.getRequestId() : null;
        if (requestId == null) {
            log.warn(LogMessage.KAFKA_MESSAGE_NULL_REQUEST_ID);
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
            return;
        }

        reportGenerationPortService.generateReport(requestId);

        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
        log.info(LogMessage.KAFKA_MESSAGE_PROCESSED, requestId);
    }
}
