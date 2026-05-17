package com.parcial_final.ms_emr.messaging;

import com.parcial_final.ms_emr.config.RabbitMQConfig;
import com.parcial_final.ms_emr.event.PatientAdmittedEvent;
import com.parcial_final.ms_emr.event.PatientDischargedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmrEventListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ADMITTED)
    public void onPatientAdmitted(PatientAdmittedEvent event) {
        log.info("[EMR] Patient admitted: id={}, name={} {}, ward={}",
                event.getPatientId(), event.getFirstName(), event.getLastName(), event.getWard());
        // TODO: create EMR record for this patient
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DISCHARGED)
    public void onPatientDischarged(PatientDischargedEvent event) {
        log.info("[EMR] Patient discharged: id={}, reason={}",
                event.getPatientId(), event.getDischargeReason());
        // TODO: close EMR record for this patient
    }
}
