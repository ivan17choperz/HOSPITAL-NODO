import { Injectable } from '@angular/core';
import { AdmissionResponse } from '../models/admission.model';
import { FhirPatient } from '../models/hc.model';

@Injectable({ providedIn: 'root' })
export class FhirHcService {

  buildEncounter(admission: AdmissionResponse): object {
    return {
      resourceType: 'Encounter',
      id: admission.id,
      status: admission.status?.toLowerCase() === 'discharged' ? 'finished' : 'in-progress',
      class: {
        system: 'http://terminology.hl7.org/CodeSystem/v3-ActCode',
        code: 'IMP',
        display: 'Inpatient encounter',
      },
      subject: { reference: `Patient/${admission.patientId}` },
      period: {
        start: admission.admittedAt,
      },
      location: [
        {
          location: { display: admission.ward },
          status: 'active',
        },
      ],
      identifier: [
        {
          system: 'http://hospital.com/identifiers/episode',
          value: `EP-${admission.id.replace(/-/g, '').substring(0, 8).toUpperCase()}`,
        },
      ],
    };
  }

  buildObservationPlaceholder(patientId: string, admissionId: string): object {
    return {
      resourceType: 'Observation',
      id: 'pending-triage-uuid',
      status: 'preliminary',
      category: [
        {
          coding: [
            {
              system: 'http://terminology.hl7.org/CodeSystem/observation-category',
              code: 'vital-signs',
              display: 'Vital Signs',
            },
          ],
        },
      ],
      code: {
        coding: [{ system: 'http://loinc.org', code: '85353-1', display: 'Vital signs panel' }],
        text: 'Signos Vitales — Pendiente de Triage',
      },
      subject: { reference: `Patient/${patientId}` },
      encounter: { reference: `Encounter/${admissionId}` },
      _note: 'Este recurso se generará cuando se registre el Triage del episodio',
    };
  }

  buildConditionPlaceholder(patientId: string, admissionId: string): object {
    return {
      resourceType: 'Condition',
      id: 'pending-emr-uuid',
      clinicalStatus: {
        coding: [{ system: 'http://terminology.hl7.org/CodeSystem/condition-clinical', code: 'active' }],
      },
      verificationStatus: {
        coding: [{ system: 'http://terminology.hl7.org/CodeSystem/condition-ver-status', code: 'unconfirmed' }],
      },
      code: { text: 'Diagnóstico pendiente — Historia Clínica no registrada' },
      subject: { reference: `Patient/${patientId}` },
      encounter: { reference: `Encounter/${admissionId}` },
      _note: 'Este recurso se generará cuando se complete la Historia Clínica del episodio',
    };
  }

  buildMedicationRequestPlaceholder(patientId: string, admissionId: string): object {
    return {
      resourceType: 'MedicationRequest',
      id: 'pending-emr-med-uuid',
      status: 'draft',
      intent: 'proposal',
      medication: {
        concept: { text: 'Medicación pendiente — Historia Clínica no registrada' },
      },
      subject: { reference: `Patient/${patientId}` },
      encounter: { reference: `Encounter/${admissionId}` },
      _note: 'Este recurso se generará cuando se registre la prescripción en la Historia Clínica',
    };
  }

  findPatientInBundle(bundle: any, patientId: string): FhirPatient | null {
    if (!bundle?.entry) return null;
    const entry = bundle.entry.find(
      (e: any) => e?.resource?.id === patientId,
    );
    return entry?.resource ?? null;
  }

  patientDisplayName(patient: FhirPatient | null): string {
    if (!patient) return 'Paciente no encontrado en FHIR';
    const name = patient.name?.[0];
    if (!name) return patient.id;
    return `${name.given?.join(' ') ?? ''} ${name.family ?? ''}`.trim();
  }
}
