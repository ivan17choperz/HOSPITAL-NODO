package com.parcial_final.ms_admission.controller;

import com.parcial_final.ms_admission.dto.AdmissionRequestDTO;
import com.parcial_final.ms_admission.dto.AdmissionResponseDTO;
import com.parcial_final.ms_admission.service.AdmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admissions")
@RequiredArgsConstructor
public class AdmissionController {

    private final AdmissionService admissionService;

    @PostMapping
    public ResponseEntity<AdmissionResponseDTO> admitPatient(@Valid @RequestBody AdmissionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(admissionService.admitPatient(request));
    }

    @GetMapping
    public List<AdmissionResponseDTO> getAllAdmissions() {
        return admissionService.getAllAdmissions();
    }

    @GetMapping("/node")
    public Map<String, String> getCurrentNode() {
        return Map.of("nodeId", admissionService.getNodeId());
    }
}
