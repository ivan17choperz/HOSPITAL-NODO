package com.parcial_final.ms_admission.repository;

import com.parcial_final.ms_admission.entity.Admission;
import com.parcial_final.ms_admission.entity.AdmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, UUID> {

    long countByStatus(AdmissionStatus status);

    long countByAdmittedAtAfter(LocalDateTime since);

    @Query("SELECT a.ward, COUNT(a) FROM Admission a WHERE a.ward IS NOT NULL GROUP BY a.ward ORDER BY COUNT(a) DESC")
    List<Object[]> countGroupedByWard();

    @Query("SELECT a.status, COUNT(a) FROM Admission a GROUP BY a.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT a.gender, COUNT(a) FROM Admission a WHERE a.gender IS NOT NULL GROUP BY a.gender")
    List<Object[]> countGroupedByGender();
}
