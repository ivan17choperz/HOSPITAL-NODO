package com.parcial_final.ms_emr.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "hospital.topic";
    public static final String QUEUE_ADMITTED = "emr.patient.admitted";
    public static final String QUEUE_DISCHARGED = "emr.patient.discharged";

    @Bean
    public TopicExchange hospitalExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue emrAdmittedQueue() {
        return new Queue(QUEUE_ADMITTED, true);
    }

    @Bean
    public Queue emrDischargedQueue() {
        return new Queue(QUEUE_DISCHARGED, true);
    }

    @Bean
    public Binding bindAdmitted(Queue emrAdmittedQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(emrAdmittedQueue).to(hospitalExchange).with("patient.admitted");
    }

    @Bean
    public Binding bindDischarged(Queue emrDischargedQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(emrDischargedQueue).to(hospitalExchange).with("patient.discharged");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
