import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { NodeService } from './node.service';
import { AdmissionResponse } from '../models/admission.model';
import { TriageCategory, TriageResponse } from '../models/triage.model';
import { EmrResponse } from '../models/emr.model';
import { NODE_COLORS } from '../models/hc.model';
import { NodeReport, ReportSnapshot, TriageStat, WardStat } from '../models/reports.model';

const TRIAGE_COLORS: Record<TriageCategory, string> = {
  RED: '#ef4444', ORANGE: '#f97316', YELLOW: '#eab308', GREEN: '#22c55e', BLUE: '#3b82f6',
};
const TRIAGE_LABELS: Record<TriageCategory, string> = {
  RED: 'Rojo', ORANGE: 'Naranja', YELLOW: 'Amarillo', GREEN: 'Verde', BLUE: 'Azul',
};
const TRIAGE_ORDER: TriageCategory[] = ['RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE'];

type Wrapped<T> = { data: T[]; online: boolean };

@Injectable({ providedIn: 'root' })
export class ReportsService {
  private readonly http = inject(HttpClient);
  private readonly nodeService = inject(NodeService);

  fetchSnapshot(): Observable<ReportSnapshot> {
    const nodes = this.nodeService.nodes;

    const wrap = <T>(obs: Observable<T[]>): Observable<Wrapped<T>> =>
      obs.pipe(
        map(data => ({ data, online: true })),
        catchError(() => of({ data: [] as T[], online: false })),
      );

    const admReqs = nodes.map(n =>
      wrap(this.http.get<AdmissionResponse[]>(`${n.gatewayUrl}/api/admission/admissions`)),
    );
    const triReqs = nodes.map(n =>
      wrap(this.http.get<TriageResponse[]>(`${n.gatewayUrl}/api/triage/triages`)),
    );
    const emrReqs = nodes.map(n =>
      wrap(this.http.get<EmrResponse[]>(`${n.gatewayUrl}/api/emr/records`)),
    );

    return forkJoin([...admReqs, ...triReqs, ...emrReqs]).pipe(
      map(results => {
        const n = nodes.length;
        const admByNode = results.slice(0, n) as Wrapped<AdmissionResponse>[];
        const triByNode = results.slice(n, 2 * n) as Wrapped<TriageResponse>[];
        const emrByNode = results.slice(2 * n) as Wrapped<EmrResponse>[];

        const nodeReports: NodeReport[] = nodes.map((node, i) => ({
          nodeId: node.id,
          nodeName: node.name,
          nodeColor: NODE_COLORS[node.id] ?? '#64748b',
          admissions: admByNode[i].data.length,
          triages: triByNode[i].data.length,
          emr: emrByNode[i].data.length,
          online: admByNode[i].online || triByNode[i].online || emrByNode[i].online,
        }));

        const allAdmissions = admByNode.flatMap(r => r.data);
        const allTriages = triByNode.flatMap(r => r.data);
        const allEmr = emrByNode.flatMap(r => r.data);

        const catCounts: Partial<Record<TriageCategory, number>> = {};
        for (const t of allTriages) {
          catCounts[t.category] = (catCounts[t.category] ?? 0) + 1;
        }

        const triageStats: TriageStat[] = TRIAGE_ORDER.map(cat => ({
          category: cat,
          label: TRIAGE_LABELS[cat],
          count: catCounts[cat] ?? 0,
          color: TRIAGE_COLORS[cat],
        }));

        const wardMap: Record<string, number> = {};
        for (const a of allAdmissions) {
          wardMap[a.ward] = (wardMap[a.ward] ?? 0) + 1;
        }
        const wardStats: WardStat[] = Object.entries(wardMap)
          .sort((a, b) => b[1] - a[1])
          .slice(0, 7)
          .map(([ward, count]) => ({ ward, count }));

        const criticalCount = (catCounts['RED'] ?? 0) + (catCounts['ORANGE'] ?? 0);
        const emrCompletionPct =
          allAdmissions.length > 0
            ? Math.round((allEmr.length / allAdmissions.length) * 100)
            : 0;

        return {
          fetchedAt: new Date(),
          nodeReports,
          triageStats,
          wardStats,
          totalAdmissions: allAdmissions.length,
          totalTriages: allTriages.length,
          totalEmr: allEmr.length,
          nodesOnline: nodeReports.filter(r => r.online).length,
          criticalCount,
          emrCompletionPct,
        };
      }),
    );
  }
}
