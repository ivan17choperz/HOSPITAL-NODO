package com.parcial_final.ms_fhir_adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalPatientDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;
    private String identifier;
}
