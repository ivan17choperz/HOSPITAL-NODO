package com.parcial_final.ms_doctor.repository;

import com.parcial_final.ms_doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
}
