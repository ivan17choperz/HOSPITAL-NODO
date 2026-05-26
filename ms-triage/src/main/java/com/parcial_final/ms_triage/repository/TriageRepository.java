package com.parcial_final.ms_triage.repository;

import com.parcial_final.ms_triage.entity.Triage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TriageRepository extends JpaRepository<Triage, UUID> {
    List<Triage> findByAdmissionId(UUID admissionId);
}
