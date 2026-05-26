package com.parcial_final.ms_fhir_adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageResponseDTO {
    private String episodeNumber;
    private String triageScale;
    private String triageLevel;
    private String observationId;
    private String observationFhirJson;
}
