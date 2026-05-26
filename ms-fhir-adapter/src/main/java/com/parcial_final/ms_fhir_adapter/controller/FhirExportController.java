package com.parcial_final.ms_fhir_adapter.controller;

import com.parcial_final.ms_fhir_adapter.dto.CorePatientRegistrationDTO;
import com.parcial_final.ms_fhir_adapter.dto.CoverageRegistrationDTO;
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

    @PostMapping(value = "/register/core", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerCorePatient(@RequestBody CorePatientRegistrationDTO patientDTO) {
        return fhirExportService.registerCorePatient(patientDTO);
    }

    @PostMapping(value = "/register/coverage", produces = MediaType.APPLICATION_JSON_VALUE)
    public String registerCoverage(@RequestBody CoverageRegistrationDTO coverageDTO) {
        return fhirExportService.registerCoverage(coverageDTO);
    }

    @GetMapping(value = "/patients", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllPatients() {
        return fhirExportService.getAllPatients();
    }
}
