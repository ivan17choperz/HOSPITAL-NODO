export interface DoctorRequest {
  firstName: string;
  lastName: string;
  specialty: string;
  licenseNumber?: string;
  email?: string;
}

export interface DoctorResponse {
  id: string;
  firstName: string;
  lastName: string;
  specialty: string;
  licenseNumber?: string;
  email?: string;
  nodeId: string;
  createdAt: string;
}
