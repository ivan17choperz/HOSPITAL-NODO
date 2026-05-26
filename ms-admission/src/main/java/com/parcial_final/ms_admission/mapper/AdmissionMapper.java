package com.parcial_final.ms_admission.mapper;

import com.parcial_final.ms_admission.dto.AdmissionRequestDTO;
import com.parcial_final.ms_admission.dto.AdmissionResponseDTO;
import com.parcial_final.ms_admission.entity.Admission;
import com.parcial_final.ms_admission.event.PatientAdmittedEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdmissionMapper {

    Admission toEntity(AdmissionRequestDTO dto);

    @Mapping(target = "status", expression = "java(admission.getStatus().name())")
    AdmissionResponseDTO toResponseDTO(Admission admission);

    @Mapping(target = "admissionId", source = "id")
    @Mapping(target = "admittedAt", source = "admittedAt")
    PatientAdmittedEvent toEvent(Admission admission);
}
