export type ClinicalStatus = 'active' | 'resolved' | 'inactive' | 'remission';
export type VerificationStatus = 'confirmed' | 'provisional' | 'differential' | 'refuted';

export interface DiagnosisEntry {
  icd10Code: string;
  description: string;
  clinicalStatus: ClinicalStatus;
  verificationStatus: VerificationStatus;
}

export interface MedicationEntry {
  medicationName: string;
  dose: string;
  unit: string;
  route: string;
  frequency: string;
  durationDays: number | null;
  notes: string;
}

export interface EmrRequest {
  admissionId: string;
  patientId: string;
  anamnesis: string;
  diagnoses: DiagnosisEntry[];
  medications: MedicationEntry[];
  clinicianNotes: string;
  doctorId?: string;
}

export interface EmrResponse {
  id: string;
  admissionId: string;
  patientId: string;
  anamnesis: string;
  diagnoses: DiagnosisEntry[];
  medications: MedicationEntry[];
  clinicianNotes: string;
  doctorId?: string;
  createdAt: string;
}

export const MEDICATION_ROUTES: { code: string; label: string; snomedCode: string }[] = [
  { code: 'oral', label: 'Oral (VO)', snomedCode: '26643006' },
  { code: 'iv', label: 'Intravenosa (IV)', snomedCode: '47625008' },
  { code: 'im', label: 'Intramuscular (IM)', snomedCode: '78421000' },
  { code: 'sc', label: 'Subcutánea (SC)', snomedCode: '34206005' },
  { code: 'topical', label: 'Tópica', snomedCode: '6064005' },
  { code: 'inhaled', label: 'Inhalatoria', snomedCode: '18679011000001101' },
  { code: 'sl', label: 'Sublingual (SL)', snomedCode: '37839007' },
];

export const FREQUENCY_OPTIONS: { code: string; label: string; timesPerDay: number }[] = [
  { code: 'od', label: 'Una vez al día (OD)', timesPerDay: 1 },
  { code: 'bid', label: 'Dos veces al día (BID)', timesPerDay: 2 },
  { code: 'tid', label: 'Tres veces al día (TID)', timesPerDay: 3 },
  { code: 'qid', label: 'Cuatro veces al día (QID)', timesPerDay: 4 },
  { code: 'q6h', label: 'Cada 6 horas (Q6H)', timesPerDay: 4 },
  { code: 'q8h', label: 'Cada 8 horas (Q8H)', timesPerDay: 3 },
  { code: 'q12h', label: 'Cada 12 horas (Q12H)', timesPerDay: 2 },
  { code: 'prn', label: 'A demanda (PRN)', timesPerDay: 0 },
];
