package com.parcial_final.ms_discharge.messaging;

import com.parcial_final.ms_discharge.config.RabbitMQConfig;
import com.parcial_final.ms_discharge.event.PatientDischargedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DischargeEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPatientDischarged(PatientDischargedEvent event) {
        log.info("Publishing patient.discharged event for patientId={}", event.getPatientId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_DISCHARGED, event);
    }
}
