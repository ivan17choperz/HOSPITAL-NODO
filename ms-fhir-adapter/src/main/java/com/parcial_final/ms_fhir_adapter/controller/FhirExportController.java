package com.parcial_final.ms_fhir_adapter.controller;

import com.parcial_final.ms_fhir_adapter.dto.CorePatientRegistrationDTO;
import com.parcial_final.ms_fhir_adapter.dto.CoverageRegistrationDTO;
import com.parcial_final.ms_fhir_adapter.dto.InternalPatientDTO;
import com.parcial_final.ms_fhir_adapter.dto.TriageRequestDTO;
import com.parcial_final.ms_fhir_adapter.dto.TriageResponseDTO;
import com.parcial_final.ms_fhir_adapter.service.FhirExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class FhirExportController {

    private final FhirExportService fhirExportService;

    @PostMapping(value = "/export/patient", produces = MediaType.APPLICATION_JSON_VALUE)
    public String exportPatient(@RequestBody InternalPatientDTO patientDTO) {
        return fhirExportService.exportPatient(patientDTO);
    }

    @PostMapping(value = "/register/core", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerCorePatient(@RequestBody CorePatientRegistrationDTO patientDTO) {
        return fhirExportService.registerCorePatient(patientDTO);
    }

    @PostMapping(value = "/register/coverage", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerCoverage(@RequestBody CoverageRegistrationDTO coverageDTO) {
        return fhirExportService.registerCoverage(coverageDTO);
    }

    @PostMapping(value = "/triage", produces = MediaType.APPLICATION_JSON_VALUE)
    public TriageResponseDTO registerTriage(@RequestBody TriageRequestDTO triageRequestDTO) {
        return fhirExportService.registerTriage(triageRequestDTO);
    }

    @GetMapping(value = "/patients", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllPatients() {
        return fhirExportService.getAllPatients();
    }

    @GetMapping(value = "/resources/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getResourcesByType(@org.springframework.web.bind.annotation.PathVariable("type") String type) {
        return fhirExportService.getResourcesByType(type);
    }

    @GetMapping(value = "/observations", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllObservations() {
        return fhirExportService.getResourcesByType("Observation");
    }
}
