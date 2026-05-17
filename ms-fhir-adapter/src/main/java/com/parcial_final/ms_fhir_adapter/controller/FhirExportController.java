package com.parcial_final.ms_fhir_adapter.controller;

import com.parcial_final.ms_fhir_adapter.dto.InternalPatientDTO;
import com.parcial_final.ms_fhir_adapter.service.FhirExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fhir")
@RequiredArgsConstructor
public class FhirExportController {

    private final FhirExportService fhirExportService;

    @PostMapping(value = "/export/patient", produces = MediaType.APPLICATION_JSON_VALUE)
    public String exportPatient(@RequestBody InternalPatientDTO patientDTO) {
        return fhirExportService.exportPatient(patientDTO);
    }

    @GetMapping(value = "/patients", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllPatients() {
        return fhirExportService.getAllPatients();
    }
}
