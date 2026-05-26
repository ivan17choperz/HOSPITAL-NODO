package com.parcial_final.ms_emr.controller;

import com.parcial_final.ms_emr.dto.ConditionRequestDTO;
import com.parcial_final.ms_emr.dto.EmrRequestDTO;
import com.parcial_final.ms_emr.dto.EmrResponse;
import com.parcial_final.ms_emr.dto.MedicationRequestDTO;
import com.parcial_final.ms_emr.repository.EmrRecordRepository;
import com.parcial_final.ms_emr.service.EmrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/emr")
@RequiredArgsConstructor
public class EmrController {

    private final EmrService emrService;
    private final EmrRecordRepository emrRecordRepository;

    @GetMapping(value = "/episodes/{episodeNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getClinicalRecord(@PathVariable String episodeNumber) {
        return emrService.getClinicalRecord(episodeNumber);
    }

    @PostMapping(value = "/episodes/{episodeNumber}/condition", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerCondition(@PathVariable String episodeNumber, @RequestBody ConditionRequestDTO dto) {
        return emrService.registerCondition(episodeNumber, dto);
    }

    @PostMapping(value = "/episodes/{episodeNumber}/medication-request", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerMedicationRequest(@PathVariable String episodeNumber, @RequestBody MedicationRequestDTO dto) {
        return emrService.registerMedicationRequest(episodeNumber, dto);
    }

    @PostMapping(value = "/records", produces = MediaType.APPLICATION_JSON_VALUE)
    public EmrResponse createRecord(@RequestBody EmrRequestDTO request) {
        return emrService.createRecord(request);
    }

    @GetMapping(value = "/records", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EmrResponse> getAllRecords() {
        return emrRecordRepository.findAll().stream()
                .map(r -> EmrResponse.builder()
                        .id(r.getId())
                        .admissionId(r.getAdmissionId() != null ? r.getAdmissionId().toString() : r.getEpisodeNumber())
                        .patientId(r.getPatientId())
                        .anamnesis(r.getAnamnesis())
                        .diagnoses(List.of())
                        .medications(List.of())
                        .clinicianNotes(r.getClinicianNotes())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/records/admission/{admissionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EmrResponse> getByAdmission(@PathVariable String admissionId) {
        return emrService.getByAdmissionId(admissionId);
    }
}
