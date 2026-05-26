import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EmrRequest, EmrResponse } from '../models/emr.model';
import { NodeService } from './node.service';

@Injectable({ providedIn: 'root' })
export class EmrService {
  private readonly http = inject(HttpClient);
  private readonly nodeService = inject(NodeService);

  private url(): string {
    return `${this.nodeService.selectedNode().gatewayUrl}/api/emr/records`;
  }

  create(request: EmrRequest): Observable<EmrResponse> {
    return this.http.post<EmrResponse>(this.url(), request);
  }

  getAll(): Observable<EmrResponse[]> {
    return this.http.get<EmrResponse[]>(this.url());
  }

  getByAdmission(admissionId: string): Observable<EmrResponse[]> {
    return this.http.get<EmrResponse[]>(`${this.url()}/admission/${admissionId}`);
  }
}
