package com.parcial_final.ms_doctor.dto;

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
public class DoctorResponseDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String specialty;
    private String licenseNumber;
    private String email;
    private String nodeId;
    private LocalDateTime createdAt;
}
