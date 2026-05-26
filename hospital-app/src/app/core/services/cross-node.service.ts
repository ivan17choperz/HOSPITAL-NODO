import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { NodeService } from './node.service';
import { AdmissionResponse } from '../models/admission.model';
import { FhirPatientBundle, NodeQueryResult, AdmissionWithNode, NODE_COLORS } from '../models/hc.model';

@Injectable({ providedIn: 'root' })
export class CrossNodeService {
  private readonly http = inject(HttpClient);
  private readonly nodeService = inject(NodeService);

  queryAdmissionsFromAllNodes(): Observable<NodeQueryResult<AdmissionWithNode>[]> {
    const requests = this.nodeService.nodes.map((node) =>
      this.http
        .get<AdmissionResponse[]>(`${node.gatewayUrl}/api/admission/admissions`)
        .pipe(
          map((data) => ({
            node,
            data: data.map((a) => ({
              ...a,
              nodeId: node.id,
              nodeName: node.name,
              nodeColor: NODE_COLORS[node.id] ?? '#64748b',
            })),
            error: null,
            loading: false,
          })),
          catchError(() =>
            of({
              node,
              data: [] as AdmissionWithNode[],
              error: 'Sin conexión',
              loading: false,
            }),
          ),
        ),
    );
    return forkJoin(requests);
  }

  queryFhirPatientsFromAllNodes(): Observable<NodeQueryResult<FhirPatientBundle>[]> {
    const requests = this.nodeService.nodes.map((node) =>
      this.http
        .get<FhirPatientBundle>(`${node.gatewayUrl}/api/fhir/patients`)
        .pipe(
          map((bundle) => ({
            node,
            data: [bundle],
            error: null,
            loading: false,
          })),
          catchError(() =>
            of({
              node,
              data: [] as FhirPatientBundle[],
              error: 'Sin conexión',
              loading: false,
            }),
          ),
        ),
    );
    return forkJoin(requests);
  }
}
