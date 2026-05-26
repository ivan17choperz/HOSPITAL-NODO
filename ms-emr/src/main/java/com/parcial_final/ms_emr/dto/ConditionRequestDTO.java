package com.parcial_final.ms_emr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionRequestDTO {
    private UUID patientId;
    private String clinicalStatus;
    private String diagnosisCode;
    private String diagnosisDisplay;
}
