package com.parcial_final.ms_fhir_adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageRegistrationDTO {
    private UUID patientId;
    private String payerName;
    private String payerIdentifier;
    private String affiliationType;
    private String affiliationNumber;
    private LocalDate affiliationStartDate;
    private LocalDate affiliationEndDate;
    private String coveragePlan;
    private String coverageLevel;
}
