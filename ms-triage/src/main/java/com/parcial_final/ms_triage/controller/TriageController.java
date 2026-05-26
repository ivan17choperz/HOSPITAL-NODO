package com.parcial_final.ms_triage.controller;

import com.parcial_final.ms_triage.dto.TriageRequestDTO;
import com.parcial_final.ms_triage.dto.TriageResponseDTO;
import com.parcial_final.ms_triage.service.TriageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class TriageController {

    private final TriageService triageService;

    @PostMapping(value = "/triages", produces = MediaType.APPLICATION_JSON_VALUE)
    public TriageResponseDTO create(@RequestBody TriageRequestDTO request) {
        return triageService.create(request);
    }

    @GetMapping(value = "/triages", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TriageResponseDTO> getAll() {
        return triageService.getAll();
    }

    @GetMapping(value = "/triages/admission/{admissionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TriageResponseDTO> getByAdmission(@PathVariable String admissionId) {
        return triageService.getByAdmissionId(admissionId);
    }
}
