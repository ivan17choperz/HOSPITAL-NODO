package com.parcial_final.ms_emr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "emr_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmrRecord {

    @Id
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "episode_number", nullable = false, unique = true)
    private String episodeNumber;

    @Column(name = "admission_id")
    private UUID admissionId;

    @Column(name = "condition_fhir_json", columnDefinition = "TEXT")
    private String conditionFhirJson;

    @Column(name = "medication_request_fhir_json", columnDefinition = "TEXT")
    private String medicationRequestFhirJson;

    @Column(name = "anamnesis", columnDefinition = "TEXT")
    private String anamnesis;

    @Column(name = "clinician_notes", columnDefinition = "TEXT")
    private String clinicianNotes;

    @Column(name = "diagnoses_json", columnDefinition = "TEXT")
    private String diagnosesJson;

    @Column(name = "medications_json", columnDefinition = "TEXT")
    private String medicationsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
