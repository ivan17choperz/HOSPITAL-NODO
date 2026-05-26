package com.parcial_final.ms_admission.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "admissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String identifier;

    private String ward;

    @Column(name = "episode_number", nullable = false, unique = true)
    private String episodeNumber;

    @Column(name = "node_id", nullable = false)
    private String nodeId;

    @Column(name = "admitted_at", nullable = false)
    private LocalDateTime admittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdmissionStatus status;
}
