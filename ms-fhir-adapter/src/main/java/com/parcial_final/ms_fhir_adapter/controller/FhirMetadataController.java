package com.parcial_final.ms_fhir_adapter.controller;

import com.parcial_final.ms_fhir_adapter.service.FhirCapabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FhirMetadataController {

    private final FhirCapabilityService fhirCapabilityService;

    @GetMapping(value = "/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getCapabilityStatement() {
        return fhirCapabilityService.getCapabilityStatement();
    }
}
