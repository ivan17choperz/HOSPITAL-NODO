package com.parcial_final.ms_fhir_adapter.service;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parcial_final.ms_fhir_adapter.dto.CorePatientRegistrationDTO;
import com.parcial_final.ms_fhir_adapter.dto.CoverageRegistrationDTO;
import com.parcial_final.ms_fhir_adapter.dto.InternalPatientDTO;
import com.parcial_final.ms_fhir_adapter.dto.TriageRequestDTO;
import com.parcial_final.ms_fhir_adapter.dto.TriageResponseDTO;
import com.parcial_final.ms_fhir_adapter.entity.FhirResourceEntity;
import com.parcial_final.ms_fhir_adapter.repository.FhirResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.Coverage;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Organization;
import org.hl7.fhir.r5.model.Patient;
import org.hl7.fhir.r5.model.Period;
import org.hl7.fhir.r5.model.Quantity;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.StringType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FhirExportService {

    private final FhirContext fhirContext;
    private final FhirResourceRepository fhirResourceRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public String exportPatient(InternalPatientDTO internalPatient) {
        Patient fhirPatient = buildFhirPatient(internalPatient);
        String payload = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(fhirPatient);
        upsertResource("Patient", fhirPatient.getIdPart(), payload);
        return payload;
    }

    @Transactional
    public String registerCorePatient(CorePatientRegistrationDTO patientDTO) {
        Patient patient = buildCoreRegistrationPatient(patientDTO);
        String payload = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        upsertResource("Patient", patient.getIdPart(), payload);
        return payload;
    }

    @Transactional
    public String registerCoverage(CoverageRegistrationDTO coverageDTO) {
        Organization payer = buildPayerOrganization(coverageDTO);
        Coverage coverage = buildCoverage(coverageDTO, payer);

        Bundle transaction = new Bundle();
        transaction.setType(Bundle.BundleType.TRANSACTION);
        transaction.addEntry()
                .setResource(payer)
                .getRequest()
                .setMethod(Bundle.HTTPVerb.POST)
                .setUrl("Organization");
        transaction.addEntry()
                .setResource(coverage)
                .getRequest()
                .setMethod(Bundle.HTTPVerb.POST)
                .setUrl("Coverage");

        String payerId = coverageDTO.getPayerIdentifier() != null ? coverageDTO.getPayerIdentifier() : payer.getName();
        upsertResource("Organization", payerId, fhirContext.newJsonParser().encodeResourceToString(payer));

        String coverageId = coverageDTO.getAffiliationNumber() != null
                ? coverageDTO.getAffiliationNumber()
                : coverageDTO.getPatientId() + "-coverage";
        upsertResource("Coverage", coverageId, fhirContext.newJsonParser().encodeResourceToString(coverage));

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(transaction);
    }

    @Transactional
    public TriageResponseDTO registerTriage(TriageRequestDTO triageDTO) {
        String scale = triageDTO.getTriageScale() == null ? "MANCHESTER" : triageDTO.getTriageScale().toUpperCase();
        String level = classifyTriageLevel(triageDTO, scale);
        Observation observation = buildTriageObservation(triageDTO, scale, level);
        String payload = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(observation);

        upsertResource("Observation", observation.getIdPart(), payload);

        return TriageResponseDTO.builder()
                .episodeNumber(triageDTO.getEpisodeNumber())
                .triageScale(scale)
                .triageLevel(level)
                .observationId(observation.getIdPart())
                .observationFhirJson(payload)
                .build();
    }

    public String getAllPatients() {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        List<FhirResourceEntity> persistedPatients = fhirResourceRepository.findAllByResourceType("Patient");
        bundle.setTotal(persistedPatients.size());
        for (FhirResourceEntity stored : persistedPatients) {
            Patient patient = fhirContext.newJsonParser().parseResource(Patient.class, stored.getPayloadJson());
            bundle.addEntry().setResource(patient);
        }
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    public String getResourcesByType(String resourceType) {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);
        List<FhirResourceEntity> persisted = fhirResourceRepository.findAllByResourceType(resourceType);
        bundle.setTotal(persisted.size());
        for (FhirResourceEntity stored : persisted) {
            try {
                org.hl7.fhir.r5.model.Resource res = (org.hl7.fhir.r5.model.Resource) fhirContext.newJsonParser()
                        .parseResource(stored.getPayloadJson());
                bundle.addEntry().setResource(res);
            } catch (Exception e) {
                log.warn("Failed to parse stored FHIR resource (type={} id={}): {}", resourceType, stored.getResourceId(), e.getMessage());
            }
        }
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }

    @Transactional
    public String registerFhirResource(String resourceType, Map<String, Object> body) {
        try {
            String fhirJson = objectMapper.writeValueAsString(body);
            var parser = fhirContext.newJsonParser().setPrettyPrint(true);
            var resource = (org.hl7.fhir.r5.model.Resource) parser.parseResource(fhirJson);
            String resourceId = resource.getIdPart();
            if (resourceId == null || resourceId.isBlank()) {
                resourceId = UUID.randomUUID().toString();
                resource.setId(resourceId);
                fhirJson = parser.encodeResourceToString(resource);
            } else {
                fhirJson = parser.encodeResourceToString(resource);
            }
            upsertResource(resourceType, resourceId, fhirJson);
            return fhirJson;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize FHIR resource body", e);
        }
    }

    public Optional<String> getResourceById(String resourceType, String resourceId) {
        return fhirResourceRepository.findByResourceTypeAndResourceId(resourceType, resourceId)
                .map(FhirResourceEntity::getPayloadJson);
    }

    @Transactional
    public String updateFhirResource(String resourceType, String resourceId, Map<String, Object> body) {
        try {
            String fhirJson = objectMapper.writeValueAsString(body);
            var parser = fhirContext.newJsonParser().setPrettyPrint(true);
            var resource = (org.hl7.fhir.r5.model.Resource) parser.parseResource(fhirJson);
            resource.setId(resourceId);
            String encoded = parser.encodeResourceToString(resource);
            upsertResource(resourceType, resourceId, encoded);
            return encoded;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize FHIR resource body", e);
        }
    }

    @Transactional
    public void deleteResource(String resourceType, String resourceId) {
        fhirResourceRepository.findByResourceTypeAndResourceId(resourceType, resourceId)
                .ifPresent(fhirResourceRepository::delete);
    }

    private Patient buildFhirPatient(InternalPatientDTO internalPatient) {
        Patient fhirPatient = new Patient();

        if (internalPatient.getId() != null) {
            fhirPatient.setId(internalPatient.getId().toString());
        }

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

    private Patient buildCoreRegistrationPatient(CorePatientRegistrationDTO dto) {
        Patient patient = new Patient();

        if (dto.getId() != null) {
            patient.setId(dto.getId().toString());
        }

        if (dto.getDocumentType() != null || dto.getDocumentNumber() != null) {
            patient.addIdentifier()
                    .setSystem("http://hospital.com/identifiers/national-document")
                    .setValue(dto.getDocumentNumber())
                    .setType(new org.hl7.fhir.r5.model.CodeableConcept().setText(dto.getDocumentType()));
        }

        HumanName officialName = patient.addName().setUse(HumanName.NameUse.OFFICIAL);
        officialName.setFamily(joinNames(dto.getLastName(), dto.getSecondLastName()));
        if (dto.getFirstName() != null) {
            officialName.addGiven(dto.getFirstName());
        }
        if (dto.getMiddleName() != null) {
            officialName.addGiven(dto.getMiddleName());
        }

        if (dto.getBirthDate() != null) {
            patient.setBirthDate(Date.from(dto.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        if (dto.getBiologicalSex() != null) {
            try {
                patient.setGender(Enumerations.AdministrativeGender.fromCode(dto.getBiologicalSex().toLowerCase()));
            } catch (Exception e) {
                patient.setGender(Enumerations.AdministrativeGender.UNKNOWN);
            }
        }

        if (dto.getGenderIdentity() != null) {
            patient.addExtension(new Extension(
                    "http://hl7.org/fhir/StructureDefinition/patient-genderIdentity",
                    new org.hl7.fhir.r5.model.CodeableConcept().setText(dto.getGenderIdentity())
            ));
        }

        if (dto.getAge() != null) {
            patient.addExtension(new Extension(
                    "http://hospital.com/fhir/StructureDefinition/patient-age-reported",
                    new StringType(String.valueOf(dto.getAge()))
            ));
        }

        if (dto.getMaritalStatus() != null) {
            patient.setMaritalStatus(new org.hl7.fhir.r5.model.CodeableConcept().setText(dto.getMaritalStatus()));
        }

        if (dto.getBirthCountry() != null || dto.getBirthState() != null || dto.getBirthCity() != null) {
            org.hl7.fhir.r5.model.Address birthPlace = new org.hl7.fhir.r5.model.Address()
                    .setCountry(dto.getBirthCountry())
                    .setState(dto.getBirthState())
                    .setCity(dto.getBirthCity());
            patient.addExtension(new Extension(
                    "http://hl7.org/fhir/StructureDefinition/patient-birthPlace",
                    birthPlace
            ));
        }

        if (dto.getAddressLine() != null || dto.getResidenceCity() != null || dto.getResidenceState() != null || dto.getResidenceCountry() != null) {
            org.hl7.fhir.r5.model.Address address = patient.addAddress();
            if (dto.getAddressLine() != null) {
                address.addLine(dto.getAddressLine());
            }
            if (dto.getAddressNumberOrApartment() != null) {
                address.addLine(dto.getAddressNumberOrApartment());
            }
            if (dto.getNeighborhood() != null) {
                address.setDistrict(dto.getNeighborhood());
            }
            address.setPostalCode(dto.getPostalCode());
            address.setCity(dto.getResidenceCity());
            address.setState(dto.getResidenceState());
            address.setCountry(dto.getResidenceCountry());
        }

        addTelecom(patient, dto.getLandlinePhone(), ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME);
        addTelecom(patient, dto.getMobilePhone(), ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE);
        addTelecom(patient, dto.getEmail(), ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.HOME);

        if (dto.getEmergencyContactFullName() != null || dto.getEmergencyPhone() != null || dto.getEmergencyRelationship() != null) {
            Patient.ContactComponent emergencyContact = patient.addContact();
            if (dto.getEmergencyContactFullName() != null) {
                emergencyContact.setName(new HumanName().setText(dto.getEmergencyContactFullName()));
            }
            if (dto.getEmergencyRelationship() != null) {
                emergencyContact.addRelationship(new org.hl7.fhir.r5.model.CodeableConcept().setText(dto.getEmergencyRelationship()));
            }
            if (dto.getEmergencyPhone() != null) {
                emergencyContact.addTelecom(new ContactPoint()
                        .setSystem(ContactPoint.ContactPointSystem.PHONE)
                        .setUse(ContactPoint.ContactPointUse.MOBILE)
                        .setValue(dto.getEmergencyPhone()));
            }
        }

        return patient;
    }

    private String joinNames(String partOne, String partTwo) {
        if (partOne == null) {
            return partTwo;
        }
        if (partTwo == null) {
            return partOne;
        }
        return partOne + " " + partTwo;
    }

    private void addTelecom(Patient patient, String value, ContactPoint.ContactPointSystem system, ContactPoint.ContactPointUse use) {
        if (value == null) {
            return;
        }
        patient.addTelecom(new ContactPoint()
                .setSystem(system)
                .setUse(use)
                .setValue(value));
    }

    private Organization buildPayerOrganization(CoverageRegistrationDTO dto) {
        Organization payer = new Organization();
        payer.setName(dto.getPayerName());

        if (dto.getPayerIdentifier() != null) {
            payer.addIdentifier(new Identifier()
                    .setSystem("http://hospital.com/identifiers/payer")
                    .setValue(dto.getPayerIdentifier()));
        }

        return payer;
    }

    private Coverage buildCoverage(CoverageRegistrationDTO dto, Organization payer) {
        Coverage coverage = new Coverage();
        coverage.setStatus(Enumerations.FinancialResourceStatusCodes.ACTIVE);

        if (dto.getAffiliationType() != null) {
            coverage.setType(new CodeableConcept().setText(dto.getAffiliationType()));
        }

        if (dto.getAffiliationNumber() != null) {
            coverage.addIdentifier(new Identifier()
                    .setSystem("http://hospital.com/identifiers/coverage-affiliation")
                    .setValue(dto.getAffiliationNumber()));
        }

        if (dto.getCoveragePlan() != null) {
            coverage.addIdentifier(new Identifier()
                    .setSystem("http://hospital.com/identifiers/coverage-plan")
                    .setValue(dto.getCoveragePlan()));
        }

        if (dto.getCoverageLevel() != null) {
            coverage.addIdentifier(new Identifier()
                    .setSystem("http://hospital.com/identifiers/coverage-level")
                    .setValue(dto.getCoverageLevel()));
        }

        if (dto.getAffiliationStartDate() != null || dto.getAffiliationEndDate() != null) {
            Period period = new Period();
            if (dto.getAffiliationStartDate() != null) {
                period.setStart(java.sql.Date.valueOf(dto.getAffiliationStartDate()));
            }
            if (dto.getAffiliationEndDate() != null) {
                period.setEnd(java.sql.Date.valueOf(dto.getAffiliationEndDate()));
            }
            coverage.setPeriod(period);
        }

        if (dto.getPatientId() != null) {
            coverage.setBeneficiary(new Reference("Patient/" + dto.getPatientId()));
        }

        if (payer.getName() != null) {
            coverage.setInsurer(new Reference("Organization?name=" + payer.getName()));
        }

        return coverage;
    }

    private void upsertResource(String resourceType, String resourceId, String payload) {
        if (resourceId == null || resourceId.isBlank()) {
            return;
        }
        Optional<FhirResourceEntity> existing = fhirResourceRepository.findByResourceTypeAndResourceId(resourceType, resourceId);
        FhirResourceEntity entity = existing.orElseGet(FhirResourceEntity::new);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setPayloadJson(payload);
        fhirResourceRepository.save(entity);
    }

    private Observation buildTriageObservation(TriageRequestDTO dto, String scale, String level) {
        Observation observation = new Observation();
        observation.setId("triage-" + UUID.randomUUID());
        observation.setStatus(Enumerations.ObservationStatus.FINAL);
        observation.setCode(new CodeableConcept().setText("Emergency triage vital signs"));

        if (dto.getPatientId() != null) {
            observation.setSubject(new Reference("Patient/" + dto.getPatientId()));
        }
        if (dto.getEpisodeNumber() != null && !dto.getEpisodeNumber().isBlank()) {
            observation.setEncounter(new Reference("Encounter/" + dto.getEpisodeNumber()));
            observation.addIdentifier(new Identifier()
                    .setSystem("http://hospital.com/identifiers/episode")
                    .setValue(dto.getEpisodeNumber()));
        }

        addQuantityComponent(observation, "Heart rate", dto.getHeartRate(), "/min");
        addQuantityComponent(observation, "Respiratory rate", dto.getRespiratoryRate(), "/min");
        addQuantityComponent(observation, "Oxygen saturation", dto.getOxygenSaturation(), "%");
        addQuantityComponent(observation, "Body temperature", dto.getTemperatureCelsius(), "Cel");
        addQuantityComponent(observation, "Systolic blood pressure", dto.getSystolicBloodPressure(), "mm[Hg]");
        addQuantityComponent(observation, "Diastolic blood pressure", dto.getDiastolicBloodPressure(), "mm[Hg]");
        addQuantityComponent(observation, "Pain scale", dto.getPainScale0To10(), "{score}");

        observation.addInterpretation(new CodeableConcept().setText(scale + " " + level));
        return observation;
    }

    private String classifyTriageLevel(TriageRequestDTO dto, String scale) {
        boolean severe = isSevere(dto);
        boolean urgent = isUrgent(dto);

        if ("ESI".equals(scale)) {
            if (severe) return "ESI-1";
            if (urgent) return "ESI-2";
            return "ESI-3";
        }

        if (severe) return "MTS-RED";
        if (urgent) return "MTS-ORANGE";
        return "MTS-YELLOW";
    }

    private boolean isSevere(TriageRequestDTO dto) {
        return (dto.getOxygenSaturation() != null && dto.getOxygenSaturation() < 90.0)
                || (dto.getSystolicBloodPressure() != null && dto.getSystolicBloodPressure() < 80)
                || (dto.getHeartRate() != null && dto.getHeartRate() > 150);
    }

    private boolean isUrgent(TriageRequestDTO dto) {
        return (dto.getOxygenSaturation() != null && dto.getOxygenSaturation() < 94.0)
                || (dto.getSystolicBloodPressure() != null && dto.getSystolicBloodPressure() < 95)
                || (dto.getHeartRate() != null && dto.getHeartRate() > 120)
                || (dto.getPainScale0To10() != null && dto.getPainScale0To10() >= 7)
                || (dto.getTemperatureCelsius() != null && dto.getTemperatureCelsius() >= 39.0);
    }

    private void addQuantityComponent(Observation observation, String label, Number value, String unit) {
        if (value == null) {
            return;
        }
        Observation.ObservationComponentComponent component = observation.addComponent();
        component.setCode(new CodeableConcept().setText(label));
        component.setValue(new Quantity()
                .setValue(value.doubleValue())
                .setUnit(unit));
    }
}
