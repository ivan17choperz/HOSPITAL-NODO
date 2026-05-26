package com.parcial_final.ms_triage.service;

import com.parcial_final.ms_triage.dto.TriageRequestDTO;
import com.parcial_final.ms_triage.dto.TriageResponseDTO;
import com.parcial_final.ms_triage.entity.Triage;
import com.parcial_final.ms_triage.repository.TriageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TriageService {

    private final TriageRepository triageRepository;

    @Transactional
    public TriageResponseDTO create(TriageRequestDTO request) {
        TriageRequestDTO.VitalSignsDTO v = request.getVitalSigns();

        Triage triage = Triage.builder()
                .admissionId(UUID.fromString(request.getAdmissionId()))
                .patientId(UUID.fromString(request.getPatientId()))
                .temperature(v != null ? v.getTemperature() : null)
                .heartRate(v != null ? v.getHeartRate() : null)
                .systolicBp(v != null ? v.getSystolicBp() : null)
                .diastolicBp(v != null ? v.getDiastolicBp() : null)
                .oxygenSaturation(v != null ? v.getOxygenSaturation() : null)
                .respiratoryRate(v != null ? v.getRespiratoryRate() : null)
                .painScore(v != null ? v.getPainScore() : null)
                .category(request.getCategory())
                .notes(request.getNotes())
                .build();

        Triage saved = triageRepository.save(triage);
        log.info("Triage registered: id={}, admissionId={}, category={}", saved.getId(), saved.getAdmissionId(), saved.getCategory());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<TriageResponseDTO> getAll() {
        return triageRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<TriageResponseDTO> getByAdmissionId(String admissionId) {
        return triageRepository.findByAdmissionId(UUID.fromString(admissionId))
                .stream().map(this::toDTO).toList();
    }

    private TriageResponseDTO toDTO(Triage t) {
        return TriageResponseDTO.builder()
                .id(t.getId().toString())
                .admissionId(t.getAdmissionId().toString())
                .patientId(t.getPatientId().toString())
                .temperature(t.getTemperature())
                .heartRate(t.getHeartRate())
                .systolicBp(t.getSystolicBp())
                .diastolicBp(t.getDiastolicBp())
                .oxygenSaturation(t.getOxygenSaturation())
                .respiratoryRate(t.getRespiratoryRate())
                .painScore(t.getPainScore())
                .category(t.getCategory())
                .notes(t.getNotes())
                .createdAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null)
                .build();
    }
}
