package com.parcial_final.ms_emr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmrRequestDTO {
    private String admissionId;
    private String patientId;
    private String anamnesis;
    private List<Map<String, Object>> diagnoses;
    private List<Map<String, Object>> medications;
    private String clinicianNotes;
}
