package com.parcial_final.ms_fhir_adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageRequestDTO {
    private UUID patientId;
    private String episodeNumber;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Double oxygenSaturation;
    private Double temperatureCelsius;
    private Integer systolicBloodPressure;
    private Integer diastolicBloodPressure;
    private Integer painScale0To10;
    private String triageScale; // "MANCHESTER" or "ESI"
}
