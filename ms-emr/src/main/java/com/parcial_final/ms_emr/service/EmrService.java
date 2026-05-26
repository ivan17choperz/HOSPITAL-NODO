package com.parcial_final.ms_emr.service;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcial_final.ms_emr.dto.ConditionRequestDTO;
import com.parcial_final.ms_emr.dto.EmrRequestDTO;
import com.parcial_final.ms_emr.dto.EmrResponse;
import com.parcial_final.ms_emr.dto.MedicationRequestDTO;
import com.parcial_final.ms_emr.entity.EmrRecord;
import com.parcial_final.ms_emr.repository.EmrRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.CodeableReference;
import org.hl7.fhir.r5.model.Condition;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.MedicationRequest;
import org.hl7.fhir.r5.model.Reference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmrService {

    private final EmrRecordRepository emrRecordRepository;
    private final FhirContext fhirContext;
    private final ObjectMapper objectMapper;

    @Transactional
    public void ensureEpisode(UUID patientId, String episodeNumber, UUID admissionId) {
        emrRecordRepository.findByEpisodeNumber(episodeNumber)
                .orElseGet(() -> emrRecordRepository.save(EmrRecord.builder()
                        .patientId(patientId)
                        .episodeNumber(episodeNumber)
                        .admissionId(admissionId)
                        .build()));
    }

    @Transactional
    public EmrResponse createRecord(EmrRequestDTO request) {
        String diagnosesJson = toJson(request.getDiagnoses());
        String medicationsJson = toJson(request.getMedications());
        UUID admissionUuid = UUID.fromString(request.getAdmissionId());

        // Prefer finding the record created by the event listener (which has the real episodeNumber)
        List<EmrRecord> byAdmission = emrRecordRepository.findByAdmissionId(admissionUuid);
        EmrRecord record;
        if (!byAdmission.isEmpty()) {
            record = byAdmission.get(0);
        } else {
            String episodeNumber = "EP-" + request.getAdmissionId().replace("-", "").substring(0, 8).toUpperCase();
            record = emrRecordRepository.findByEpisodeNumber(episodeNumber).orElseGet(EmrRecord::new);
            if (record.getEpisodeNumber() == null) {
                record.setEpisodeNumber(episodeNumber);
            }
        }

        record.setPatientId(UUID.fromString(request.getPatientId()));
        record.setAdmissionId(admissionUuid);
        record.setAnamnesis(request.getAnamnesis());
        record.setClinicianNotes(request.getClinicianNotes());
        record.setDiagnosesJson(diagnosesJson);
        record.setMedicationsJson(medicationsJson);

        EmrRecord saved = emrRecordRepository.save(record);
        log.info("EMR record saved: id={}, admissionId={}, episode={}", saved.getId(), saved.getAdmissionId(), saved.getEpisodeNumber());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EmrResponse> getByAdmissionId(String admissionId) {
        return emrRecordRepository.findByAdmissionId(UUID.fromString(admissionId))
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public String registerCondition(String episodeNumber, ConditionRequestDTO dto) {
        EmrRecord record = getRecordOrThrow(episodeNumber);
        Condition condition = buildCondition(record.getEpisodeNumber(), dto);
        String json = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(condition);
        record.setConditionFhirJson(json);
        emrRecordRepository.save(record);
        return json;
    }

    @Transactional
    public String registerMedicationRequest(String episodeNumber, MedicationRequestDTO dto) {
        EmrRecord record = getRecordOrThrow(episodeNumber);
        MedicationRequest medicationRequest = buildMedicationRequest(record.getEpisodeNumber(), dto);
        String json = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(medicationRequest);
        record.setMedicationRequestFhirJson(json);
        emrRecordRepository.save(record);
        return json;
    }

    @Transactional(readOnly = true)
    public String getClinicalRecord(String episodeNumber) {
        EmrRecord record = getRecordOrThrow(episodeNumber);
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);

        if (record.getConditionFhirJson() != null) {
            bundle.addEntry().setResource(fhirContext.newJsonParser()
                    .parseResource(Condition.class, record.getConditionFhirJson()));
        }
        if (record.getMedicationRequestFhirJson() != null) {
            bundle.addEntry().setResource(fhirContext.newJsonParser()
                    .parseResource(MedicationRequest.class, record.getMedicationRequestFhirJson()));
        }
        bundle.setTotal(bundle.getEntry().size());
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    private EmrRecord getRecordOrThrow(String episodeNumber) {
        return emrRecordRepository.findByEpisodeNumber(episodeNumber)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Episode not found: " + episodeNumber));
    }

    private EmrResponse toResponse(EmrRecord r) {
        return EmrResponse.builder()
                .id(r.getId())
                .admissionId(r.getAdmissionId() != null ? r.getAdmissionId().toString() : r.getEpisodeNumber())
                .patientId(r.getPatientId())
                .anamnesis(r.getAnamnesis())
                .diagnoses(fromJson(r.getDiagnosesJson()))
                .medications(fromJson(r.getMedicationsJson()))
                .clinicianNotes(r.getClinicianNotes())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<Object> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
        } catch (Exception e) {
            return List.of();
        }
    }

    private Condition buildCondition(String episodeNumber, ConditionRequestDTO dto) {
        Condition condition = new Condition();
        condition.setId("condition-" + UUID.randomUUID());
        condition.setClinicalStatus(new CodeableConcept().setText(
                dto.getClinicalStatus() == null ? "active" : dto.getClinicalStatus()));
        condition.setCode(new CodeableConcept().setText(
                dto.getDiagnosisDisplay() == null ? dto.getDiagnosisCode() : dto.getDiagnosisDisplay()));
        if (dto.getDiagnosisCode() != null) {
            condition.addIdentifier(new Identifier()
                    .setSystem("http://hospital.com/diagnosis-code")
                    .setValue(dto.getDiagnosisCode()));
        }
        condition.setSubject(new Reference("Patient/" + dto.getPatientId()));
        condition.setEncounter(new Reference("Encounter/" + episodeNumber));
        return condition;
    }

    private MedicationRequest buildMedicationRequest(String episodeNumber, MedicationRequestDTO dto) {
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setId("medreq-" + UUID.randomUUID());
        medicationRequest.setStatus(MedicationRequest.MedicationrequestStatus.ACTIVE);
        medicationRequest.setIntent(parseIntent(dto.getIntent()));
        medicationRequest.setMedication(new CodeableReference(new CodeableConcept().setText(
                dto.getMedicationDisplay() == null ? dto.getMedicationCode() : dto.getMedicationDisplay())));
        if (dto.getMedicationCode() != null) {
            medicationRequest.addIdentifier(new Identifier()
                    .setSystem("http://hospital.com/medication-code")
                    .setValue(dto.getMedicationCode()));
        }
        medicationRequest.setSubject(new Reference("Patient/" + dto.getPatientId()));
        medicationRequest.setEncounter(new Reference("Encounter/" + episodeNumber));
        if (dto.getDosageInstruction() != null) {
            medicationRequest.addDosageInstruction().setText(dto.getDosageInstruction());
        }
        return medicationRequest;
    }

    private MedicationRequest.MedicationRequestIntent parseIntent(String intent) {
        if (intent == null) {
            return MedicationRequest.MedicationRequestIntent.ORDER;
        }
        return switch (intent.toLowerCase()) {
            case "plan" -> MedicationRequest.MedicationRequestIntent.PLAN;
            case "proposal" -> MedicationRequest.MedicationRequestIntent.PROPOSAL;
            default -> MedicationRequest.MedicationRequestIntent.ORDER;
        };
    }
}
