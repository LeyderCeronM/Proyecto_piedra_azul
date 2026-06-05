package com.piedrazul.professionals.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MEDICAL_EXCHANGE = "medical.exchange";

    public static final String PROFESSIONALS_QUEUE = "professionals.requests";
    public static final String PROFESSIONALS_RESPONSES_QUEUE = "professionals.responses";

    public static final String PROFESSIONALS_REQUEST_ROUTING_KEY = "professionals.request";
    public static final String PROFESSIONALS_RESPONSE_ROUTING_KEY = "professionals.response";
    public static final String PROFESSIONALS_CREATED_ROUTING_KEY = "professionals.created";
    public static final String PROFESSIONALS_UPDATED_ROUTING_KEY = "professionals.updated";

    @Bean
    public TopicExchange medicalExchange() {
        return new TopicExchange(MEDICAL_EXCHANGE, true, false);
    }

    @Bean
    public Queue professionalsRequestsQueue() {
        return QueueBuilder.durable(PROFESSIONALS_QUEUE)
                .withArgument("x-message-ttl", 30000)
                .build();
    }

    @Bean
    public Queue professionalsResponsesQueue() {
        return QueueBuilder.durable(PROFESSIONALS_RESPONSES_QUEUE)
                .withArgument("x-message-ttl", 30000)
                .build();
    }

    @Bean
    public Binding professionalsRequestsBinding() {
        return BindingBuilder
                .bind(professionalsRequestsQueue())
                .to(medicalExchange())
                .with(PROFESSIONALS_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding professionalsResponsesBinding() {
        return BindingBuilder
                .bind(professionalsResponsesQueue())
                .to(medicalExchange())
                .with(PROFESSIONALS_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setExchange(MEDICAL_EXCHANGE);
        return template;
    }
}
