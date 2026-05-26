package com.parcial_final.ms_triage.dto;

import com.parcial_final.ms_triage.entity.TriageCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriageRequestDTO {

    private String admissionId;
    private String patientId;
    private VitalSignsDTO vitalSigns;
    private TriageCategory category;
    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VitalSignsDTO {
        private Double temperature;
        private Integer heartRate;
        private Integer systolicBp;
        private Integer diastolicBp;
        private Double oxygenSaturation;
        private Integer respiratoryRate;
        private Integer painScore;
    }
}
