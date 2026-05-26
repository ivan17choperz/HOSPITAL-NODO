import { Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { CrossNodeService } from '../../../core/services/cross-node.service';
import { FhirHcService } from '../../../core/services/fhir-hc.service';
import { NodeService } from '../../../core/services/node.service';
import {
  AdmissionWithNode,
  FhirPatientBundle,
  NodeQueryResult,
} from '../../../core/models/hc.model';
import { FhirPatient } from '../../../core/models/hc.model';

type FhirTab = 'patient' | 'encounter' | 'observation' | 'condition' | 'medication';

@Component({
  selector: 'app-hc-view',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './hc-view.html',
  styleUrl: './hc-view.css',
})
export class HcViewComponent {
  private readonly crossNode = inject(CrossNodeService);
  private readonly fhirHc = inject(FhirHcService);
  private readonly http = inject(HttpClient);
  readonly nodeService = inject(NodeService);

  readonly nodeResults = signal<NodeQueryResult<AdmissionWithNode>[]>([]);
  readonly fhirNodeResults = signal<NodeQueryResult<FhirPatientBundle>[]>([]);
  readonly querying = signal(false);
  readonly selectedAdmission = signal<AdmissionWithNode | null>(null);
  readonly fhirTab = signal<FhirTab>('patient');

  readonly observationBundle = signal<any | null>(null);
  readonly clinicalBundle = signal<any | null>(null);

  readonly allAdmissions = computed<AdmissionWithNode[]>(() =>
    this.nodeResults().flatMap((r) => r.data),
  );

  readonly totalNodes = computed(() => this.nodeResults().length);
  readonly nodesUp = computed(
    () => this.nodeResults().filter((r) => r.error === null && r.data.length >= 0).length,
  );

  readonly selectedFhirPatient = computed<FhirPatient | null>(() => {
    const admission = this.selectedAdmission();
    if (!admission) return null;
    for (const result of this.fhirNodeResults()) {
      if (result.data.length > 0) {
        const found = this.fhirHc.findPatientInBundle(result.data[0], admission.patientId);
        if (found) return found;
      }
    }
    return null;
  });

  readonly fhirPreview = computed(() => {
    const admission = this.selectedAdmission();
    if (!admission) return '// Seleccione un episodio';
    const patientId = admission.patientId;
    const admissionId = admission.id;
    switch (this.fhirTab()) {
      case 'patient':
        return this.selectedFhirPatient()
          ? JSON.stringify(this.selectedFhirPatient(), null, 2)
          : `// Patient/${patientId} — No encontrado en FHIR adapter\n// El paciente se crea automáticamente al registrar la admisión`;
      case 'encounter':
        return JSON.stringify(this.fhirHc.buildEncounter(admission), null, 2);
      case 'observation':
        return JSON.stringify(
          this.fhirHc.buildObservationPlaceholder(patientId, admissionId),
          null,
          2,
        );
      case 'condition':
        return JSON.stringify(
          this.fhirHc.buildConditionPlaceholder(patientId, admissionId),
          null,
          2,
        );
      case 'medication':
        return JSON.stringify(
          this.fhirHc.buildMedicationRequestPlaceholder(patientId, admissionId),
          null,
          2,
        );
    }
  });

  readonly fhirTabStatus = computed(() => ({
    patient: this.selectedFhirPatient() ? 'ok' : 'warn',
    encounter: 'derived',
    observation: 'pending',
    condition: 'pending',
    medication: 'pending',
  }));

  queryAllNodes(): void {
    this.querying.set(true);
    this.selectedAdmission.set(null);
    this.nodeResults.set(
      this.nodeService.nodes.map((n) => ({
        node: n,
        data: [],
        error: null,
        loading: true,
      })),
    );

    this.crossNode.queryAdmissionsFromAllNodes().subscribe((results) => {
      this.nodeResults.set(results);
      this.crossNode.queryFhirPatientsFromAllNodes().subscribe((fhirResults) => {
        this.fhirNodeResults.set(fhirResults);
        this.querying.set(false);
      });
    });
  }

  selectAdmission(admission: AdmissionWithNode): void {
    this.selectedAdmission.set(admission);
    this.fhirTab.set('patient');
    this.observationBundle.set(null);
    this.clinicalBundle.set(null);

    const gateway = admission.node.gatewayUrl;
    // fetch observations from fhir adapter for that node
    this.http.get(`${gateway}/api/fhir/observations`).subscribe({
      next: (b) => this.observationBundle.set(b),
      error: () => this.observationBundle.set(null),
    });

    // fetch EMR clinical record from emr service (episode)
    this.http.get(`${gateway}/api/emr/episodes/${admission.episodeNumber}`).subscribe({
      next: (b) => this.clinicalBundle.set(b),
      error: () => this.clinicalBundle.set(null),
    });
  }

  setFhirTab(tab: FhirTab): void {
    this.fhirTab.set(tab);
  }

  episodeNumber(id: string): string {
    return `EP-${id.replace(/-/g, '').substring(0, 8).toUpperCase()}`;
  }

  patientName(patient: FhirPatient | null): string {
    return this.fhirHc.patientDisplayName(patient);
  }

  deduplicateByPatient(): { patientId: string; nodes: string[] }[] {
    const map = new Map<string, Set<string>>();
    for (const a of this.allAdmissions()) {
      const set = map.get(a.patientId) ?? new Set();
      set.add(a.nodeName);
      map.set(a.patientId, set);
    }
    return [...map.entries()].map(([patientId, nodes]) => ({
      patientId,
      nodes: [...nodes],
    }));
  }
}
