package com.parcial_final.ms_triage.dto;

import com.parcial_final.ms_triage.entity.TriageCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriageResponseDTO {
    private String id;
    private String admissionId;
    private String patientId;
    private Double temperature;
    private Integer heartRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Double oxygenSaturation;
    private Integer respiratoryRate;
    private Integer painScore;
    private TriageCategory category;
    private String notes;
    private String createdAt;
}
