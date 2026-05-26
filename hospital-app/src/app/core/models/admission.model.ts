export interface HospitalNode {
  id: string;
  name: string;
  gatewayUrl: string;
}

export interface AdmissionRequest {
  patientId: string;
  firstName: string;
  lastName: string;
  gender: string;
  birthDate: string;
  identifier: string;
  ward: string;
}

export interface AdmissionResponse {
  id: string;
  patientId: string;
  firstName: string;
  lastName: string;
  gender: string;
  birthDate: string;
  identifier: string;
  ward: string;
  episodeNumber: string;
  admittedAt: string;
  status: string;
}
