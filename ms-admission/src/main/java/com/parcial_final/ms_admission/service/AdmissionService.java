package com.parcial_final.ms_admission.service;

import com.parcial_final.ms_admission.dto.AdmissionRequestDTO;
import com.parcial_final.ms_admission.dto.AdmissionResponseDTO;
import com.parcial_final.ms_admission.dto.ReportSummaryDTO;
import com.parcial_final.ms_admission.entity.Admission;
import com.parcial_final.ms_admission.entity.AdmissionStatus;
import com.parcial_final.ms_admission.mapper.AdmissionMapper;
import com.parcial_final.ms_admission.messaging.AdmissionEventPublisher;
import com.parcial_final.ms_admission.repository.AdmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final AdmissionMapper admissionMapper;
    private final AdmissionEventPublisher eventPublisher;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Value("${node.id:node1}")
    private String nodeId;

    @Transactional
    public AdmissionResponseDTO admitPatient(AdmissionRequestDTO request) {
        if (request.getTargetNodeId() != null && !request.getTargetNodeId().isBlank()
                && !nodeId.equalsIgnoreCase(request.getTargetNodeId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This node is '" + nodeId + "' but request targets '" + request.getTargetNodeId() + "'.");
        }

        Admission admission = admissionMapper.toEntity(request);
        admission.setAdmittedAt(LocalDateTime.now());
        admission.setStatus(AdmissionStatus.ADMITTED);
        admission.setNodeId(nodeId);
        admission.setEpisodeNumber(resolveEpisodeNumber(request.getEpisodeNumber()));

        Admission saved = admissionRepository.save(admission);
        log.info("Patient admitted: id={}, patientId={}, ward={}, episode={}, node={}",
                saved.getId(), saved.getPatientId(), saved.getWard(), saved.getEpisodeNumber(), saved.getNodeId());

        eventPublisher.publishPatientAdmitted(admissionMapper.toEvent(saved));

        return admissionMapper.toResponseDTO(saved);
    }

    public List<AdmissionResponseDTO> getAllAdmissions() {
        return circuitBreakerFactory.create("admission-db").run(
                () -> admissionRepository.findAll().stream().map(admissionMapper::toResponseDTO).toList(),
                ex -> {
                    log.warn("DB circuit open — returning empty list: {}", ex.getMessage());
                    return Collections.emptyList();
                }
        );
    }

    public String getNodeId() {
        return nodeId;
    }

    public ReportSummaryDTO getReportSummary() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        Map<String, Long> byWard = new LinkedHashMap<>();
        for (Object[] row : admissionRepository.countGroupedByWard()) {
            byWard.put((String) row[0], (Long) row[1]);
        }

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : admissionRepository.countGroupedByStatus()) {
            byStatus.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, Long> byGender = new LinkedHashMap<>();
        for (Object[] row : admissionRepository.countGroupedByGender()) {
            byGender.put((String) row[0], (Long) row[1]);
        }

        return ReportSummaryDTO.builder()
                .nodeId(nodeId)
                .generatedAt(LocalDateTime.now())
                .totalAdmissions(admissionRepository.count())
                .admissionsToday(admissionRepository.countByAdmittedAtAfter(startOfToday))
                .activeAdmissions(admissionRepository.countByStatus(AdmissionStatus.ADMITTED))
                .admissionsByWard(byWard)
                .admissionsByStatus(byStatus)
                .admissionsByGender(byGender)
                .build();
    }

    private String resolveEpisodeNumber(String requestedEpisodeNumber) {
        if (requestedEpisodeNumber != null && !requestedEpisodeNumber.isBlank()) {
            return requestedEpisodeNumber;
        }
        return "EPI-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
    }
}
