package com.parcial_final.ms_fhir_adapter.service;

import ca.uhn.fhir.context.FhirContext;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.CodeType;
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
        cs.setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE);
        cs.setFhirVersion(Enumerations.FHIRVersion._5_0_0);
        cs.addFormat(new CodeType("application/fhir+json"));
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
                .setDocumentation("Returned when querying all patients. Bundle type: searchset.");

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
