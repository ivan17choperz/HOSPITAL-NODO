package com.parcial_final.ms_laboratory.messaging;

import com.parcial_final.ms_laboratory.config.RabbitMQConfig;
import com.parcial_final.ms_laboratory.event.PatientAdmittedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LaboratoryEventListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ADMITTED)
    public void onPatientAdmitted(PatientAdmittedEvent event) {
        log.info("[LAB] Patient admitted: id={}, name={} {}",
                event.getPatientId(), event.getFirstName(), event.getLastName());
        // TODO: initialize lab profile for this patient
    }
}
