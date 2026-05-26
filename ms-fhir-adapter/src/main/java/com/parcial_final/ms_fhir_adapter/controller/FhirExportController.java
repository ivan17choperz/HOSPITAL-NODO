package com.parcial_final.ms_fhir_adapter.controller;

import com.parcial_final.ms_fhir_adapter.dto.CorePatientRegistrationDTO;
import com.parcial_final.ms_fhir_adapter.dto.CoverageRegistrationDTO;
import com.parcial_final.ms_fhir_adapter.dto.InternalPatientDTO;
import com.parcial_final.ms_fhir_adapter.dto.TriageRequestDTO;
import com.parcial_final.ms_fhir_adapter.dto.TriageResponseDTO;
import com.parcial_final.ms_fhir_adapter.service.FhirExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @PostMapping(value = "/resources/{type}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createResource(
            @org.springframework.web.bind.annotation.PathVariable("type") String type,
            @RequestBody Map<String, Object> body) {
        String created = fhirExportService.registerFhirResource(type, body);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping(value = "/observations", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAllObservations() {
        return fhirExportService.getResourcesByType("Observation");
    }

    @PostMapping(value = "/condition", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerCondition(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(fhirExportService.registerFhirResource("Condition", body));
    }

    @PostMapping(value = "/medication-request", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerMedicationRequest(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(fhirExportService.registerFhirResource("MedicationRequest", body));
    }

    @GetMapping(value = "/patients/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPatient(@PathVariable("id") String id) {
        return fhirExportService.getResourceById("Patient", id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/observations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getObservation(@PathVariable("id") String id) {
        return fhirExportService.getResourceById("Observation", id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/resources/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getResource(
            @PathVariable("type") String type,
            @PathVariable("id") String id) {
        return fhirExportService.getResourceById(type, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @org.springframework.web.bind.annotation.PutMapping(
            value = "/resources/{type}/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateResource(
            @PathVariable("type") String type,
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> body) {
        String updated = fhirExportService.updateFhirResource(type, id, body);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/resources/{type}/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable("type") String type, @PathVariable("id") String id) {
        fhirExportService.deleteResource(type, id);
        return ResponseEntity.noContent().build();
    }
}
