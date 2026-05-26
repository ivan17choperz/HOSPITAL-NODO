package com.parcial_final.ms_admission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AdmissionRequestDTO {

    @NotNull
    private UUID patientId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String gender;

    private LocalDate birthDate;

    private String identifier;

    @NotBlank
    private String ward;
}
