package com.parcial_final.ms_doctor.service;

import com.parcial_final.ms_doctor.dto.DoctorRequestDTO;
import com.parcial_final.ms_doctor.dto.DoctorResponseDTO;
import com.parcial_final.ms_doctor.entity.Doctor;
import com.parcial_final.ms_doctor.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Value("${node.id:node1}")
    private String nodeId;

    @Transactional
    public DoctorResponseDTO create(DoctorRequestDTO request) {
        Doctor doctor = Doctor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialty(request.getSpecialty())
                .licenseNumber(request.getLicenseNumber())
                .email(request.getEmail())
                .nodeId(nodeId)
                .build();

        Doctor saved = doctorRepository.save(doctor);
        log.info("Doctor registered: id={}, name={} {}, specialty={}", saved.getId(), saved.getFirstName(), saved.getLastName(), saved.getSpecialty());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAll() {
        return doctorRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponseDTO getById(UUID id) {
        return doctorRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found: " + id));
    }

    private DoctorResponseDTO toDTO(Doctor d) {
        return DoctorResponseDTO.builder()
                .id(d.getId())
                .firstName(d.getFirstName())
                .lastName(d.getLastName())
                .specialty(d.getSpecialty())
                .licenseNumber(d.getLicenseNumber())
                .email(d.getEmail())
                .nodeId(d.getNodeId())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
