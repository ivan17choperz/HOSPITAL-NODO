package com.parcial_final.ms_emr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrResponse {
    private UUID id;
    private String admissionId;
    private UUID patientId;
    private String anamnesis;
    private List<Object> diagnoses;
    private List<Object> medications;
    private String clinicianNotes;
    private LocalDateTime createdAt;
}
