package com.parcial_final.ms_laboratory.config;

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
    public static final String QUEUE_ADMITTED = "lab.patient.admitted";

    @Bean
    public TopicExchange hospitalExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue labAdmittedQueue() {
        return new Queue(QUEUE_ADMITTED, true);
    }

    @Bean
    public Binding bindAdmitted(Queue labAdmittedQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(labAdmittedQueue).to(hospitalExchange).with("patient.admitted");
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
