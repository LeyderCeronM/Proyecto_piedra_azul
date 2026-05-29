package com.medical.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for appointments-service.
 * Declares the same exchange, queues, and bindings as users-service
 * so both services share the messaging infrastructure.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange name (shared with users-service)
    public static final String MEDICAL_EXCHANGE = "medical.exchange";

    // Queue names (shared with users-service)
    public static final String PATIENT_VALIDATION_REQUESTS_QUEUE = "patient.validation.requests";
    public static final String PATIENT_VALIDATION_RESPONSES_QUEUE = "patient.validation.responses";

    // Routing keys (shared with users-service)
    public static final String VALIDATION_REQUEST_ROUTING_KEY = "validation.request";
    public static final String VALIDATION_RESPONSE_ROUTING_KEY = "validation.response";

    /**
     * Medical topic exchange for all medical services communication.
     */
    @Bean
    public TopicExchange medicalExchange() {
        return new TopicExchange(MEDICAL_EXCHANGE, true, false);
    }

    /**
     * Queue for sending patient validation requests to users-service.
     */
    @Bean
    public Queue patientValidationRequestsQueue() {
        return QueueBuilder.durable(PATIENT_VALIDATION_REQUESTS_QUEUE)
                .withArgument("x-message-ttl", 30000) // 30 seconds TTL
                .build();
    }

    /**
     * Queue for receiving patient validation responses from users-service.
     */
    @Bean
    public Queue patientValidationResponsesQueue() {
        return QueueBuilder.durable(PATIENT_VALIDATION_RESPONSES_QUEUE)
                .withArgument("x-message-ttl", 30000) // 30 seconds TTL
                .build();
    }

    /**
     * Binding for validation requests queue.
     */
    @Bean
    public Binding patientValidationRequestsBinding() {
        return BindingBuilder
                .bind(patientValidationRequestsQueue())
                .to(medicalExchange())
                .with(VALIDATION_REQUEST_ROUTING_KEY);
    }

    /**
     * Binding for validation responses queue.
     */
    @Bean
    public Binding patientValidationResponsesBinding() {
        return BindingBuilder
                .bind(patientValidationResponsesQueue())
                .to(medicalExchange())
                .with(VALIDATION_RESPONSE_ROUTING_KEY);
    }

    /**
     * JSON message converter for RabbitMQ.
     */
    @Bean
    public MessageConverter jsonMessageConverter(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate for publishing messages.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setExchange(MEDICAL_EXCHANGE);
        return template;
    }
}
