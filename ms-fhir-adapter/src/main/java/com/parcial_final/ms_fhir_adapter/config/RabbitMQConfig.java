package com.parcial_final.ms_fhir_adapter.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "hospital.topic";
    public static final String QUEUE_ADMITTED = "fhir.patient.admitted";
    public static final String QUEUE_DISCHARGED = "fhir.patient.discharged";

    @Bean
    public TopicExchange hospitalExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue fhirAdmittedQueue() {
        return new Queue(QUEUE_ADMITTED, true);
    }

    @Bean
    public Queue fhirDischargedQueue() {
        return new Queue(QUEUE_DISCHARGED, true);
    }

    @Bean
    public Binding bindAdmitted(Queue fhirAdmittedQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(fhirAdmittedQueue).to(hospitalExchange).with("patient.admitted");
    }

    @Bean
    public Binding bindDischarged(Queue fhirDischargedQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(fhirDischargedQueue).to(hospitalExchange).with("patient.discharged");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
