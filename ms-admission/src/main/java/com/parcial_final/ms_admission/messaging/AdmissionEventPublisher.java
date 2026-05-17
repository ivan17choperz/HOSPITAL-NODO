package com.parcial_final.ms_admission.messaging;

import com.parcial_final.ms_admission.config.RabbitMQConfig;
import com.parcial_final.ms_admission.event.PatientAdmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdmissionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPatientAdmitted(PatientAdmittedEvent event) {
        log.info("Publishing patient.admitted event for patientId={}", event.getPatientId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_ADMITTED, event);
    }
}
