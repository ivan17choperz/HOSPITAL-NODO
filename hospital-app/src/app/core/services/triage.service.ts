import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TriageRequest, TriageResponse } from '../models/triage.model';
import { NodeService } from './node.service';

@Injectable({ providedIn: 'root' })
export class TriageService {
  private readonly http = inject(HttpClient);
  private readonly nodeService = inject(NodeService);

  private triagesUrl(): string {
    return `${this.nodeService.selectedNode().gatewayUrl}/api/triage/triages`;
  }

  create(request: TriageRequest): Observable<TriageResponse> {
    return this.http.post<TriageResponse>(this.triagesUrl(), request);
  }

  getAll(): Observable<TriageResponse[]> {
    return this.http.get<TriageResponse[]>(this.triagesUrl());
  }

  getByAdmission(admissionId: string): Observable<TriageResponse[]> {
    return this.http.get<TriageResponse[]>(`${this.triagesUrl()}/admission/${admissionId}`);
  }
}
