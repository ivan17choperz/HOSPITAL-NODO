import { Injectable } from '@angular/core';
import { DiagnosisEntry, MedicationEntry, MEDICATION_ROUTES, FREQUENCY_OPTIONS } from '../models/emr.model';

@Injectable({ providedIn: 'root' })
export class FhirEmrService {
  buildCondition(diagnosis: DiagnosisEntry, patientId: string, admissionId: string): object {
    return {
      resourceType: 'Condition',
      id: crypto.randomUUID(),
      clinicalStatus: {
        coding: [
          {
            system: 'http://terminology.hl7.org/CodeSystem/condition-clinical',
            code: diagnosis.clinicalStatus,
          },
        ],
      },
      verificationStatus: {
        coding: [
          {
            system: 'http://terminology.hl7.org/CodeSystem/condition-ver-status',
            code: diagnosis.verificationStatus,
          },
        ],
      },
      code: {
        coding: [
          {
            system: 'http://hl7.org/fhir/sid/icd-10',
            code: diagnosis.icd10Code || 'Z99',
            display: diagnosis.description,
          },
        ],
        text: diagnosis.description,
      },
      subject: { reference: `Patient/${patientId}` },
      encounter: { reference: `Encounter/${admissionId}` },
      onsetDateTime: new Date().toISOString(),
      recordedDate: new Date().toISOString().split('T')[0],
    };
  }

  buildMedicationRequest(med: MedicationEntry, patientId: string, admissionId: string): object {
    const routeInfo = MEDICATION_ROUTES.find((r) => r.code === med.route);
    const freqInfo = FREQUENCY_OPTIONS.find((f) => f.code === med.frequency);

    const dosageInstruction: Record<string, unknown> = {
      text: `${med.dose} ${med.unit} ${freqInfo?.label ?? med.frequency}${med.durationDays ? ` por ${med.durationDays} días` : ''}`,
    };

    if (routeInfo) {
      dosageInstruction['route'] = {
        coding: [
          {
            system: 'http://snomed.info/sct',
            code: routeInfo.snomedCode,
            display: routeInfo.label,
          },
        ],
      };
    }

    if (freqInfo && freqInfo.timesPerDay > 0) {
      dosageInstruction['timing'] = {
        repeat: { frequency: freqInfo.timesPerDay, period: 1, periodUnit: 'd' },
      };
    }

    if (med.dose) {
      dosageInstruction['doseAndRate'] = [
        { doseQuantity: { value: parseFloat(med.dose) || med.dose, unit: med.unit } },
      ];
    }

    const resource: Record<string, unknown> = {
      resourceType: 'MedicationRequest',
      id: crypto.randomUUID(),
      status: 'active',
      intent: 'order',
      medication: {
        concept: {
          coding: [
            {
              system: 'http://www.nlm.nih.gov/research/umls/rxnorm',
              display: med.medicationName,
            },
          ],
          text: `${med.medicationName} ${med.dose} ${med.unit}`.trim(),
        },
      },
      subject: { reference: `Patient/${patientId}` },
      encounter: { reference: `Encounter/${admissionId}` },
      authoredOn: new Date().toISOString(),
      dosageInstruction: [dosageInstruction],
    };

    if (med.durationDays) {
      resource['dispenseRequest'] = {
        validityPeriod: {
          start: new Date().toISOString().split('T')[0],
          end: new Date(Date.now() + med.durationDays * 86400000).toISOString().split('T')[0],
        },
      };
    }

    if (med.notes) {
      resource['note'] = [{ text: med.notes }];
    }

    return resource;
  }

  buildBundle(
    diagnoses: DiagnosisEntry[],
    medications: MedicationEntry[],
    patientId: string,
    admissionId: string,
  ): object {
    const entries: object[] = [
      ...diagnoses
        .filter((d) => d.description || d.icd10Code)
        .map((d) => ({ resource: this.buildCondition(d, patientId, admissionId) })),
      ...medications
        .filter((m) => m.medicationName)
        .map((m) => ({ resource: this.buildMedicationRequest(m, patientId, admissionId) })),
    ];

    return {
      resourceType: 'Bundle',
      type: 'transaction',
      timestamp: new Date().toISOString(),
      total: entries.length,
      entry: entries,
    };
  }
}
