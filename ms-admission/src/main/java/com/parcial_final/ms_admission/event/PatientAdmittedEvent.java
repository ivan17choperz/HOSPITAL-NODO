package com.parcial_final.ms_admission.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientAdmittedEvent {
    private UUID admissionId;
    private UUID patientId;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;
    private String identifier;
    private String ward;
    private String episodeNumber;
    private String nodeId;
    private LocalDateTime admittedAt;
}
