package com.parcial_final.ms_admission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDTO {

    private String nodeId;
    private LocalDateTime generatedAt;

    // Métrica 1 — total de admisiones
    private long totalAdmissions;

    // Métrica 2 — admisiones hoy
    private long admissionsToday;

    // Métrica 3 — admisiones activas (status = ADMITTED)
    private long activeAdmissions;

    // Métrica 4 — distribución por sala/ward
    private Map<String, Long> admissionsByWard;

    // Métrica 5 — distribución por estado
    private Map<String, Long> admissionsByStatus;

    // Métrica 6 — distribución por género
    private Map<String, Long> admissionsByGender;
}
