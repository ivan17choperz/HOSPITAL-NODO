package com.parcial_final.ms_fhir_adapter.service;

import ca.uhn.fhir.context.FhirContext;
import com.parcial_final.ms_fhir_adapter.dto.InternalPatientDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Patient;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class FhirExportService {

    private final FhirContext fhirContext;
    private final List<Patient> patientStore = new CopyOnWriteArrayList<>();

    public String exportPatient(InternalPatientDTO internalPatient) {
        Patient fhirPatient = buildFhirPatient(internalPatient);
        patientStore.removeIf(p -> internalPatient.getId().equals(p.getId()));
        patientStore.add(fhirPatient);
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirPatient);
    }

    public String getAllPatients() {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        bundle.setTotal(patientStore.size());
        for (Patient patient : patientStore) {
            bundle.addEntry().setResource(patient);
        }
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    private Patient buildFhirPatient(InternalPatientDTO internalPatient) {
        Patient fhirPatient = new Patient();

        fhirPatient.setId(internalPatient.getId());

        fhirPatient.addIdentifier()
                .setSystem("http://hospital.com/identifiers/patient")
                .setValue(internalPatient.getIdentifier());

        fhirPatient.addName()
                .setFamily(internalPatient.getLastName())
                .addGiven(internalPatient.getFirstName());

        if (internalPatient.getGender() != null) {
            try {
                fhirPatient.setGender(Enumerations.AdministrativeGender.fromCode(internalPatient.getGender().toLowerCase()));
            } catch (Exception e) {
                log.warn("Invalid gender code: {}. Defaulting to unknown.", internalPatient.getGender());
                fhirPatient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }

        if (internalPatient.getBirthDate() != null) {
            fhirPatient.setBirthDate(Date.from(internalPatient.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        return fhirPatient;
    }
}
