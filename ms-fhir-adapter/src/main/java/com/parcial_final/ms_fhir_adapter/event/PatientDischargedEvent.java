package com.parcial_final.ms_fhir_adapter.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDischargedEvent {
    private String patientId;
    private String firstName;
    private String lastName;
    private String dischargeReason;
    private LocalDateTime dischargedAt;
}
