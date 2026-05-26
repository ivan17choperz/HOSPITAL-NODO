package com.parcial_final.ms_fhir_adapter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorePatientRegistrationDTO {
    private UUID id;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String secondLastName;
    private LocalDate birthDate;
    private Integer age;
    private String biologicalSex;
    private String genderIdentity;
    private String maritalStatus;
    private String birthCountry;
    private String birthState;
    private String birthCity;

    private String addressLine;
    private String addressNumberOrApartment;
    private String neighborhood;
    private String postalCode;
    private String residenceCountry;
    private String residenceState;
    private String residenceCity;
    private String landlinePhone;
    private String mobilePhone;
    private String email;

    private String emergencyContactFullName;
    private String emergencyRelationship;
    private String emergencyPhone;
}
