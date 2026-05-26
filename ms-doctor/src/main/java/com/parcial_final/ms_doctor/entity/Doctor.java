package com.parcial_final.ms_doctor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "doctors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    private UUID id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String specialty;

    @Column(name = "license_number", unique = true)
    private String licenseNumber;

    private String email;

    @Column(name = "node_id", nullable = false)
    private String nodeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
