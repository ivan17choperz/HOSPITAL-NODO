package com.parcial_final.ms_admission.service;

import com.parcial_final.ms_admission.dto.AdmissionRequestDTO;
import com.parcial_final.ms_admission.dto.AdmissionResponseDTO;
import com.parcial_final.ms_admission.entity.Admission;
import com.parcial_final.ms_admission.entity.AdmissionStatus;
import com.parcial_final.ms_admission.mapper.AdmissionMapper;
import com.parcial_final.ms_admission.messaging.AdmissionEventPublisher;
import com.parcial_final.ms_admission.repository.AdmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final AdmissionMapper admissionMapper;
    private final AdmissionEventPublisher eventPublisher;

    @Transactional
    public AdmissionResponseDTO admitPatient(AdmissionRequestDTO request) {
        Admission admission = admissionMapper.toEntity(request);
        admission.setAdmittedAt(LocalDateTime.now());
        admission.setStatus(AdmissionStatus.ADMITTED);

        Admission saved = admissionRepository.save(admission);
        log.info("Patient admitted: id={}, patientId={}, ward={}", saved.getId(), saved.getPatientId(), saved.getWard());

        eventPublisher.publishPatientAdmitted(admissionMapper.toEvent(saved));

        return admissionMapper.toResponseDTO(saved);
    }

    public List<AdmissionResponseDTO> getAllAdmissions() {
        return admissionRepository.findAll()
                .stream()
                .map(admissionMapper::toResponseDTO)
                .toList();
    }
}
