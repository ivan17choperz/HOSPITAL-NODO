import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NodeService } from './node.service';

@Injectable({ providedIn: 'root' })
export class FhirService {
  private readonly http = inject(HttpClient);
  private readonly nodeService = inject(NodeService);

  private fhirUrl(): string {
    return `${this.nodeService.selectedNode().gatewayUrl}/api/fhir`;
  }

  registerTriage(request: {
    patientId: string;
    episodeNumber: string;
    heartRate?: number | null;
    respiratoryRate?: number | null;
    oxygenSaturation?: number | null;
    temperatureCelsius?: number | null;
    systolicBloodPressure?: number | null;
    diastolicBloodPressure?: number | null;
    painScale0To10?: number | null;
    triageScale?: string;
  }): Observable<unknown> {
    return this.http.post<unknown>(`${this.fhirUrl()}/triage`, request);
  }

  registerCondition(fhirResource: object): Observable<string> {
    return this.http.post(`${this.fhirUrl()}/condition`, fhirResource, { responseType: 'text' });
  }

  registerMedicationRequest(fhirResource: object): Observable<string> {
    return this.http.post(`${this.fhirUrl()}/medication-request`, fhirResource, { responseType: 'text' });
  }

  getPatients(): Observable<string> {
    return this.http.get(`${this.fhirUrl()}/patients`, { responseType: 'text' });
  }

  getObservations(): Observable<string> {
    return this.http.get(`${this.fhirUrl()}/observations`, { responseType: 'text' });
  }

  getPatient(id: string): Observable<string> {
    return this.http.get(`${this.fhirUrl()}/patients/${id}`, { responseType: 'text' });
  }

  getObservation(id: string): Observable<string> {
    return this.http.get(`${this.fhirUrl()}/observations/${id}`, { responseType: 'text' });
  }

  deleteResource(type: string, id: string): Observable<void> {
    return this.http.delete<void>(`${this.fhirUrl()}/resources/${type}/${id}`);
  }
}
