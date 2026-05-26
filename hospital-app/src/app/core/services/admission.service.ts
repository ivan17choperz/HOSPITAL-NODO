import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdmissionRequest, AdmissionResponse } from '../models/admission.model';
import { NodeService } from './node.service';

@Injectable({ providedIn: 'root' })
export class AdmissionService {
  private readonly http = inject(HttpClient);
  private readonly nodeService = inject(NodeService);

  private admissionsUrl(): string {
    return `${this.nodeService.selectedNode().gatewayUrl}/api/admission/admissions`;
  }

  create(request: AdmissionRequest): Observable<AdmissionResponse> {
    return this.http.post<AdmissionResponse>(this.admissionsUrl(), request);
  }

  getAll(): Observable<AdmissionResponse[]> {
    return this.http.get<AdmissionResponse[]>(this.admissionsUrl());
  }
}
