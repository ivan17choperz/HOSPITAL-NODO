import { HospitalNode } from './admission.model';
import { AdmissionResponse } from './admission.model';

export interface NodeQueryResult<T> {
  node: HospitalNode;
  data: T[];
  error: string | null;
  loading: boolean;
}

export interface FhirPatientBundle {
  resourceType: string;
  type: string;
  total: number;
  entry?: { resource: FhirPatient }[];
}

export interface FhirPatient {
  resourceType: string;
  id: string;
  identifier?: { system: string; value: string }[];
  name?: { family: string; given: string[] }[];
  gender?: string;
  birthDate?: string;
}

export interface AdmissionWithNode extends AdmissionResponse {
  nodeId: string;
  nodeName: string;
  nodeColor: string;
}

export const NODE_COLORS: Record<string, string> = {
  node1: '#1d4ed8',
  node2: '#7c3aed',
  node3: '#0f766e',
};
