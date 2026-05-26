package com.parcial_final.ms_emr.messaging;

import com.parcial_final.ms_emr.config.RabbitMQConfig;
import com.parcial_final.ms_emr.event.PatientAdmittedEvent;
import com.parcial_final.ms_emr.event.PatientDischargedEvent;
import com.parcial_final.ms_emr.service.EmrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmrEventListener {

    private final EmrService emrService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ADMITTED)
    public void onPatientAdmitted(PatientAdmittedEvent event) {
        log.info("[EMR] Patient admitted: id={}, name={} {}, ward={}",
                event.getPatientId(), event.getFirstName(), event.getLastName(), event.getWard());
        if (event.getEpisodeNumber() != null && event.getPatientId() != null) {
            emrService.ensureEpisode(event.getPatientId(), event.getEpisodeNumber(), event.getAdmissionId());
            log.info("[EMR] Episode {} initialized for patient {}", event.getEpisodeNumber(), event.getPatientId());
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DISCHARGED)
    public void onPatientDischarged(PatientDischargedEvent event) {
        log.info("[EMR] Patient discharged: id={}, reason={}",
                event.getPatientId(), event.getDischargeReason());
        // TODO: close EMR record for this patient
    }
}
