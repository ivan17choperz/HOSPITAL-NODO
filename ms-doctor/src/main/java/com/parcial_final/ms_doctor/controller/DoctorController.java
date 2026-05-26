package com.parcial_final.ms_doctor.controller;

import com.parcial_final.ms_doctor.dto.DoctorRequestDTO;
import com.parcial_final.ms_doctor.dto.DoctorResponseDTO;
import com.parcial_final.ms_doctor.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/doctors")
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponseDTO create(@Valid @RequestBody DoctorRequestDTO request) {
        return doctorService.create(request);
    }

    @GetMapping("/doctors")
    public List<DoctorResponseDTO> getAll() {
        return doctorService.getAll();
    }

    @GetMapping("/doctors/{id}")
    public DoctorResponseDTO getById(@PathVariable UUID id) {
        return doctorService.getById(id);
    }
}
