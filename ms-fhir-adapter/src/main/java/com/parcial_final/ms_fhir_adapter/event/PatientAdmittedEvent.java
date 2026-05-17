package com.parcial_final.ms_fhir_adapter.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientAdmittedEvent {
    private String patientId;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;
    private String identifier;
    private String ward;
    private LocalDateTime admittedAt;
}
