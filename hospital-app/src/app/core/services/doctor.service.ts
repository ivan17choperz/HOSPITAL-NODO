import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DoctorRequest, DoctorResponse } from '../models/doctor.model';
import { NodeService } from './node.service';

@Injectable({ providedIn: 'root' })
export class DoctorService {
  private readonly http = inject(HttpClient);
  private readonly nodeService = inject(NodeService);

  private url(): string {
    return `${this.nodeService.selectedNode().gatewayUrl}/api/doctor/doctors`;
  }

  create(request: DoctorRequest): Observable<DoctorResponse> {
    return this.http.post<DoctorResponse>(this.url(), request);
  }

  getAll(): Observable<DoctorResponse[]> {
    return this.http.get<DoctorResponse[]>(this.url());
  }

  getById(id: string): Observable<DoctorResponse> {
    return this.http.get<DoctorResponse>(`${this.url()}/${id}`);
  }
}
