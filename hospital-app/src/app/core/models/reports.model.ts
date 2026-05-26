import { TriageCategory } from './triage.model';

export interface NodeReport {
  nodeId: string;
  nodeName: string;
  nodeColor: string;
  admissions: number;
  triages: number;
  emr: number;
  online: boolean;
}

export interface TriageStat {
  category: TriageCategory;
  label: string;
  count: number;
  color: string;
}

export interface WardStat {
  ward: string;
  count: number;
}

export interface ReportSnapshot {
  fetchedAt: Date;
  nodeReports: NodeReport[];
  triageStats: TriageStat[];
  wardStats: WardStat[];
  totalAdmissions: number;
  totalTriages: number;
  totalEmr: number;
  nodesOnline: number;
  criticalCount: number;
  emrCompletionPct: number;
}
