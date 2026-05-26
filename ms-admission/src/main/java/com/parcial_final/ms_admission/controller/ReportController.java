package com.parcial_final.ms_admission.controller;

import com.parcial_final.ms_admission.dto.ReportSummaryDTO;
import com.parcial_final.ms_admission.service.AdmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final AdmissionService admissionService;

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDTO> getSummary() {
        return ResponseEntity.ok(admissionService.getReportSummary());
    }
}
