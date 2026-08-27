package es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import es.NTTEnterprise.RIntellix.ms_reporting.infrastructure.adapters.input.kafka.dtos.ScoringResultMessageDTO;
import es.NTTEnterprise.RIntellix.ms_reporting.utils.LogMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka consumer configuration for the persistScoring topic.
 * ms-reporting consumes the same topic as ms-core-data, but with its OWN
 * consumer group so both services receive a copy of every message. The retry
 * back-off is what tolerates the race condition with ms-core-data: while the
 * scoring is not yet queryable the listener throws a retryable exception and
 * the message is redelivered.
 * 
 * @author Lucía Fernández Mancebo
 * @date 29/06/2026
 */
@Configuration
@Slf4j
@EnableKafka
public class KafkaConsumerConfig {

    private static final String AUTO_OFFSET_RESET_EARLIEST = "earliest";
    private static final String TRUSTED_PACKAGES_ALL = "*";
    private static final int MAX_POLL_RECORDS = 1;
    private static final int SESSION_TIMEOUT_MS = 30000;
    private static final int SINGLE_CONSUMER_CONCURRENCY = 1;

    private final String bootstrapServers;
    private final String groupId;
    private final int maxRetryAttempts;
    private final long initialDelayMs;

    /**
     * Constructs a KafkaConsumerConfig with its external properties.
     *
     * @param bootstrapServers the Kafka broker address
     * @param groupId          the consumer group identifier
     * @param maxRetryAttempts the maximum number of retry attempts for message
     *                         delivery
     * @param initialDelayMs   the backoff delay between retry attempts
     */
    public KafkaConsumerConfig(
            @Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers,
            @Value("${scoring.kafka.consumer.group-id}") final String groupId,
            @Value("${scoring.kafka.consumer.retry.max-attempts}") final int maxRetryAttempts,
            @Value("${scoring.kafka.consumer.retry.initial-delay}") final long initialDelayMs) {
        this.bootstrapServers = Objects.requireNonNull(bootstrapServers);
        this.groupId = Objects.requireNonNull(groupId);
        this.maxRetryAttempts = maxRetryAttempts;
        this.initialDelayMs = initialDelayMs;
    }

    /**
     * Declares the consumer factory for parsing scoring messages.
     *
     * @return the consumer factory instance
     */
    @Bean
    public ConsumerFactory<String, ScoringResultMessageDTO> consumerFactory() {
        final Map<String, Object> configProps = new HashMap<>();

        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ScoringResultMessageDTO.class.getName());
        configProps.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configProps.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, TRUSTED_PACKAGES_ALL);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET_EARLIEST);
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS);

        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JacksonJsonDeserializer.class.getName());
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class.getName());

        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Declares the listener container factory configuration.
     *
     * @param consumerFactory the consumer factory instance
     * @return the ConcurrentKafkaListenerContainerFactory instance
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ScoringResultMessageDTO> kafkaListenerContainerFactory(
            final ConsumerFactory<String, ScoringResultMessageDTO> consumerFactory) {

        final ConcurrentKafkaListenerContainerFactory<String, ScoringResultMessageDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setCommonErrorHandler(kafkaErrorHandler());
        factory.setConcurrency(SINGLE_CONSUMER_CONCURRENCY);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConsumerFactory(consumerFactory);

        return factory;
    }

    /**
     * Wires manual validation configurations to the listener container.
     *
     * @param validator the local validator factory bean
     * @return the KafkaListenerConfigurer instance
     */
    @Bean
    public KafkaListenerConfigurer kafkaListenerConfigurer(final LocalValidatorFactoryBean validator) {
        return registrar -> {
            final DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
            factory.setValidator(validator);
            factory.afterPropertiesSet();
            registrar.setMessageHandlerMethodFactory(factory);
        };
    }

    /**
     * Registers validator post processor bean.
     *
     * @param validator the local validator factory bean
     * @return the MethodValidationPostProcessor instance
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor(final LocalValidatorFactoryBean validator) {
        final MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator);
        return processor;
    }

    /**
     * Declares the default retry error handler.
     *
     * @return the CommonErrorHandler instance
     */
    @Bean
    public CommonErrorHandler kafkaErrorHandler() {

        final FixedBackOff backoff = new FixedBackOff(initialDelayMs, maxRetryAttempts);

        final DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            log.error(LogMessage.KAFKA_CONSUMER_ERROR,
                    consumerRecord.offset(), exception.getMessage(), exception);
            log.error(LogMessage.KAFKA_MAX_RETRIES_REACHED, consumerRecord.offset());
        }, backoff);

        // Ensure that the message offset is committed once max retries are exhausted to
        // break the loop
        errorHandler.setCommitRecovered(true);
        errorHandler.addNotRetryableExceptions(MethodArgumentNotValidException.class);
        return errorHandler;
    }
}
