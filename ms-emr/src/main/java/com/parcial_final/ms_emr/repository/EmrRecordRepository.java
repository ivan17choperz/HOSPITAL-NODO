package com.parcial_final.ms_emr.repository;

import com.parcial_final.ms_emr.entity.EmrRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmrRecordRepository extends JpaRepository<EmrRecord, UUID> {
    Optional<EmrRecord> findByEpisodeNumber(String episodeNumber);
    List<EmrRecord> findByAdmissionId(UUID admissionId);
}
