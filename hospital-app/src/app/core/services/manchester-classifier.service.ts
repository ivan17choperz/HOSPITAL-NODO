import { Injectable } from '@angular/core';
import { TriageCategory, VitalSigns } from '../models/triage.model';

@Injectable({ providedIn: 'root' })
export class ManchesterClassifierService {
  classify(v: VitalSigns): TriageCategory {
    // RED — amenaza vital inmediata
    if (v.oxygenSaturation !== null && v.oxygenSaturation < 90) return 'RED';
    if (v.heartRate !== null && (v.heartRate < 40 || v.heartRate > 150)) return 'RED';
    if (v.systolicBp !== null && v.systolicBp < 70) return 'RED';
    if (v.respiratoryRate !== null && (v.respiratoryRate < 8 || v.respiratoryRate > 30)) return 'RED';
    if (v.temperature !== null && v.temperature > 41.0) return 'RED';

    // ORANGE — muy urgente
    if (v.oxygenSaturation !== null && v.oxygenSaturation < 94) return 'ORANGE';
    if (v.heartRate !== null && (v.heartRate < 50 || v.heartRate > 130)) return 'ORANGE';
    if (v.systolicBp !== null && v.systolicBp < 90) return 'ORANGE';
    if (v.temperature !== null && (v.temperature < 35.0 || v.temperature > 39.5)) return 'ORANGE';
    if (v.respiratoryRate !== null && (v.respiratoryRate < 10 || v.respiratoryRate > 25)) return 'ORANGE';
    if (v.painScore !== null && v.painScore >= 8) return 'ORANGE';

    // YELLOW — urgente
    if (v.oxygenSaturation !== null && v.oxygenSaturation < 96) return 'YELLOW';
    if (v.heartRate !== null && v.heartRate > 100) return 'YELLOW';
    if (v.systolicBp !== null && v.systolicBp < 100) return 'YELLOW';
    if (v.temperature !== null && v.temperature > 38.5) return 'YELLOW';
    if (v.respiratoryRate !== null && v.respiratoryRate > 20) return 'YELLOW';
    if (v.painScore !== null && v.painScore >= 5) return 'YELLOW';

    // GREEN — normal
    if (v.painScore !== null && v.painScore >= 3) return 'GREEN';
    if (v.temperature !== null && v.temperature > 37.5) return 'GREEN';

    // BLUE — no urgente
    return 'BLUE';
  }

  buildFhirObservation(
    triageId: string,
    patientId: string,
    admissionId: string,
    v: VitalSigns,
    category: TriageCategory,
  ): object {
    const components: object[] = [];

    if (v.temperature !== null) {
      components.push({
        code: { coding: [{ system: 'http://loinc.org', code: '8310-5', display: 'Body temperature' }] },
        valueQuantity: { value: v.temperature, unit: '°C', system: 'http://unitsofmeasure.org', code: 'Cel' },
      });
    }
    if (v.heartRate !== null) {
      components.push({
        code: { coding: [{ system: 'http://loinc.org', code: '8867-4', display: 'Heart rate' }] },
        valueQuantity: { value: v.heartRate, unit: 'lpm', system: 'http://unitsofmeasure.org', code: '/min' },
      });
    }
    if (v.systolicBp !== null) {
      components.push({
        code: { coding: [{ system: 'http://loinc.org', code: '8480-6', display: 'Systolic blood pressure' }] },
        valueQuantity: { value: v.systolicBp, unit: 'mmHg', system: 'http://unitsofmeasure.org', code: 'mm[Hg]' },
      });
    }
    if (v.diastolicBp !== null) {
      components.push({
        code: { coding: [{ system: 'http://loinc.org', code: '8462-4', display: 'Diastolic blood pressure' }] },
        valueQuantity: { value: v.diastolicBp, unit: 'mmHg', system: 'http://unitsofmeasure.org', code: 'mm[Hg]' },
      });
    }
    if (v.oxygenSaturation !== null) {
      components.push({
        code: { coding: [{ system: 'http://loinc.org', code: '59408-5', display: 'Oxygen saturation' }] },
        valueQuantity: { value: v.oxygenSaturation, unit: '%', system: 'http://unitsofmeasure.org', code: '%' },
      });
    }
    if (v.respiratoryRate !== null) {
      components.push({
        code: { coding: [{ system: 'http://loinc.org', code: '9279-1', display: 'Respiratory rate' }] },
        valueQuantity: { value: v.respiratoryRate, unit: 'resp/min', system: 'http://unitsofmeasure.org', code: '/min' },
      });
    }
    if (v.painScore !== null) {
      components.push({
        code: { coding: [{ system: 'http://loinc.org', code: '72514-3', display: 'Pain severity' }] },
        valueInteger: v.painScore,
      });
    }

    return {
      resourceType: 'Observation',
      id: triageId,
      status: 'final',
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
        text: 'Signos Vitales — Triage',
      },
      subject: { reference: `Patient/${patientId}` },
      encounter: { reference: `Encounter/${admissionId}` },
      effectiveDateTime: new Date().toISOString(),
      extension: [
        {
          url: 'http://hospital.com/fhir/StructureDefinition/manchester-triage-category',
          valueCode: category,
        },
      ],
      component: components,
    };
  }
}
