package com.parcial_final.ms_discharge.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDischargedEvent {
    private UUID patientId;
    private String firstName;
    private String lastName;
    private String dischargeReason;
    private LocalDateTime dischargedAt;
}
