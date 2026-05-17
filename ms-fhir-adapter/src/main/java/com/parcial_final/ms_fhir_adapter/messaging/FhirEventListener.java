package com.parcial_final.ms_fhir_adapter.messaging;

import com.parcial_final.ms_fhir_adapter.config.RabbitMQConfig;
import com.parcial_final.ms_fhir_adapter.dto.InternalPatientDTO;
import com.parcial_final.ms_fhir_adapter.event.PatientAdmittedEvent;
import com.parcial_final.ms_fhir_adapter.event.PatientDischargedEvent;
import com.parcial_final.ms_fhir_adapter.service.FhirExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FhirEventListener {

    private final FhirExportService fhirExportService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ADMITTED)
    public void onPatientAdmitted(PatientAdmittedEvent event) {
        log.info("[FHIR] Received patient.admitted for patientId={}", event.getPatientId());
        InternalPatientDTO dto = InternalPatientDTO.builder()
                .id(event.getPatientId())
                .firstName(event.getFirstName())
                .lastName(event.getLastName())
                .gender(event.getGender())
                .birthDate(event.getBirthDate())
                .identifier(event.getIdentifier())
                .build();
        fhirExportService.exportPatient(dto);
        log.info("[FHIR] Patient {} converted and stored as FHIR resource", event.getPatientId());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DISCHARGED)
    public void onPatientDischarged(PatientDischargedEvent event) {
        log.info("[FHIR] Received patient.discharged for patientId={}", event.getPatientId());
        // Patient discharge is recorded — FHIR resource remains in store
    }
}
