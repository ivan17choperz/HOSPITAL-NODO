package com.parcial_final.ms_fhir_adapter.service;

import ca.uhn.fhir.context.FhirContext;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Enumerations;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class FhirCapabilityService {

    private final FhirContext fhirContext;

    public String getCapabilityStatement() {
        CapabilityStatement cs = new CapabilityStatement();

        cs.setStatus(Enumerations.PublicationStatus.ACTIVE);
        cs.setDateElement(new DateTimeType(new Date()));
        cs.setKind(Enumerations.CapabilityStatementKind.INSTANCE);
        cs.setFhirVersion(Enumerations.FHIRVersion._5_0_0);
        cs.addFormat("application/fhir+json");
        cs.setTitle("Hospital FHIR Adapter - Capability Statement");
        cs.setName("HospitalFhirAdapter");
        cs.setDescription("FHIR R5 adapter for the hospital distributed system. Exposes patient data in FHIR-compliant format.");
        cs.setPublisher("Hospital Node - Distributed Systems Project");
        cs.setSoftware(
                new CapabilityStatement.CapabilityStatementSoftwareComponent()
                        .setName("ms-fhir-adapter")
                        .setVersion("0.0.1-SNAPSHOT")
        );

        CapabilityStatement.CapabilityStatementRestComponent rest = cs.addRest();
        rest.setMode(CapabilityStatement.RestfulCapabilityMode.SERVER);

        // Patient resource
        CapabilityStatement.CapabilityStatementRestResourceComponent patientResource =
                rest.addResource();
        patientResource.setType("Patient");
        patientResource.setProfile("http://hl7.org/fhir/StructureDefinition/Patient");

        // GET all patients (search)
        patientResource.addInteraction()
                .setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE)
                .setDocumentation("Returns a FHIR Bundle containing all patients registered in the system. Endpoint: GET /api/fhir/patients");

        // POST export patient
        patientResource.addInteraction()
                .setCode(CapabilityStatement.TypeRestfulInteraction.CREATE)
                .setDocumentation("Converts an internal patient DTO to a FHIR Patient resource and stores it. Endpoint: POST /api/fhir/export/patient");

        // Bundle resource
        CapabilityStatement.CapabilityStatementRestResourceComponent bundleResource =
                rest.addResource();
        bundleResource.setType("Bundle");
        bundleResource.setProfile("http://hl7.org/fhir/StructureDefinition/Bundle");
        bundleResource.addInteraction()
                .setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE)
                .setDocumentation("Returned when querying resources by type. Bundle type: searchset.");

        // Condition resource (ICD-10 diagnoses)
        CapabilityStatement.CapabilityStatementRestResourceComponent conditionResource =
                rest.addResource();
        conditionResource.setType("Condition");
        conditionResource.setProfile("http://hl7.org/fhir/StructureDefinition/Condition");
        conditionResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.CREATE)
                .setDocumentation("POST /api/fhir/condition");
        conditionResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ)
                .setDocumentation("GET /api/fhir/resources/Condition/{id}");
        conditionResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.UPDATE)
                .setDocumentation("PUT /api/fhir/resources/Condition/{id}");
        conditionResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.DELETE)
                .setDocumentation("DELETE /api/fhir/resources/Condition/{id}");
        conditionResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE)
                .setDocumentation("GET /api/fhir/resources/Condition");

        // MedicationRequest resource
        CapabilityStatement.CapabilityStatementRestResourceComponent medResource =
                rest.addResource();
        medResource.setType("MedicationRequest");
        medResource.setProfile("http://hl7.org/fhir/StructureDefinition/MedicationRequest");
        medResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.CREATE)
                .setDocumentation("POST /api/fhir/medication-request");
        medResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ)
                .setDocumentation("GET /api/fhir/resources/MedicationRequest/{id}");
        medResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.UPDATE)
                .setDocumentation("PUT /api/fhir/resources/MedicationRequest/{id}");
        medResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.DELETE)
                .setDocumentation("DELETE /api/fhir/resources/MedicationRequest/{id}");
        medResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE)
                .setDocumentation("GET /api/fhir/resources/MedicationRequest");

        // Observation resource (triage vital signs)
        CapabilityStatement.CapabilityStatementRestResourceComponent obsResource =
                rest.addResource();
        obsResource.setType("Observation");
        obsResource.setProfile("http://hl7.org/fhir/StructureDefinition/Observation");
        obsResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.CREATE)
                .setDocumentation("POST /api/fhir/triage");
        obsResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ)
                .setDocumentation("GET /api/fhir/observations/{id}");
        obsResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.UPDATE)
                .setDocumentation("PUT /api/fhir/resources/Observation/{id}");
        obsResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.DELETE)
                .setDocumentation("DELETE /api/fhir/resources/Observation/{id}");
        obsResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE)
                .setDocumentation("GET /api/fhir/observations");

        // Encounter resource (hospital admissions)
        CapabilityStatement.CapabilityStatementRestResourceComponent encResource =
                rest.addResource();
        encResource.setType("Encounter");
        encResource.setProfile("http://hl7.org/fhir/StructureDefinition/Encounter");
        encResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.CREATE)
                .setDocumentation("POST /api/fhir/resources/Encounter (generic endpoint)");
        encResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.READ)
                .setDocumentation("GET /api/fhir/resources/Encounter/{id}");
        encResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.UPDATE)
                .setDocumentation("PUT /api/fhir/resources/Encounter/{id}");
        encResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.DELETE)
                .setDocumentation("DELETE /api/fhir/resources/Encounter/{id}");
        encResource.addInteraction().setCode(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE)
                .setDocumentation("GET /api/fhir/resources/Encounter");

        // CapabilityStatement itself (metadata)
        CapabilityStatement.CapabilityStatementRestResourceComponent metaResource =
                rest.addResource();
        metaResource.setType("CapabilityStatement");
        metaResource.addInteraction()
                .setCode(CapabilityStatement.TypeRestfulInteraction.READ)
                .setDocumentation("Returns this CapabilityStatement. Endpoint: GET /metadata");

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(cs);
    }
}
