export type TriageCategory = 'RED' | 'ORANGE' | 'YELLOW' | 'GREEN' | 'BLUE';

export interface TriageCategoryInfo {
  code: TriageCategory;
  label: string;
  description: string;
  maxWaitMinutes: number | null;
  colorClass: string;
}

export const TRIAGE_CATEGORIES: Record<TriageCategory, TriageCategoryInfo> = {
  RED: {
    code: 'RED',
    label: 'Rojo — Inmediato',
    description: 'Amenaza vital inmediata. Atención inmediata.',
    maxWaitMinutes: 0,
    colorClass: 'cat-red',
  },
  ORANGE: {
    code: 'ORANGE',
    label: 'Naranja — Muy Urgente',
    description: 'Situación grave. Atención en máx. 10 minutos.',
    maxWaitMinutes: 10,
    colorClass: 'cat-orange',
  },
  YELLOW: {
    code: 'YELLOW',
    label: 'Amarillo — Urgente',
    description: 'Estado estable con riesgo. Atención en máx. 60 minutos.',
    maxWaitMinutes: 60,
    colorClass: 'cat-yellow',
  },
  GREEN: {
    code: 'GREEN',
    label: 'Verde — Normal',
    description: 'Urgencia menor. Atención en máx. 120 minutos.',
    maxWaitMinutes: 120,
    colorClass: 'cat-green',
  },
  BLUE: {
    code: 'BLUE',
    label: 'Azul — No Urgente',
    description: 'Consulta diferible. Atención en máx. 240 minutos.',
    maxWaitMinutes: 240,
    colorClass: 'cat-blue',
  },
};

export interface VitalSigns {
  temperature: number | null;       // °C
  heartRate: number | null;         // lpm
  systolicBp: number | null;        // mmHg
  diastolicBp: number | null;       // mmHg
  oxygenSaturation: number | null;  // %
  respiratoryRate: number | null;   // resp/min
  painScore: number | null;         // 0–10
}

export interface TriageRequest {
  admissionId: string;
  patientId: string;
  vitalSigns: VitalSigns;
  category: TriageCategory;
  notes: string;
}

export interface TriageResponse {
  id: string;
  admissionId: string;
  patientId: string;
  temperature: number | null;
  heartRate: number | null;
  systolicBp: number | null;
  diastolicBp: number | null;
  oxygenSaturation: number | null;
  respiratoryRate: number | null;
  painScore: number | null;
  category: TriageCategory;
  notes: string;
  createdAt: string;
}
