package com.parcial_final.ms_fhir_adapter.repository;

import com.parcial_final.ms_fhir_adapter.entity.FhirResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FhirResourceRepository extends JpaRepository<FhirResourceEntity, UUID> {
    Optional<FhirResourceEntity> findByResourceTypeAndResourceId(String resourceType, String resourceId);
    List<FhirResourceEntity> findAllByResourceType(String resourceType);
}
