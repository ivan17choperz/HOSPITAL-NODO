package com.parcial_final.ms_admission.repository;

import com.parcial_final.ms_admission.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, String> {
}
