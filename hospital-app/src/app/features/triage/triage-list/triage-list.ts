import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { TriageService } from '../../../core/services/triage.service';
import { NodeService } from '../../../core/services/node.service';
import { TriageResponse, TRIAGE_CATEGORIES } from '../../../core/models/triage.model';

@Component({
  selector: 'app-triage-list',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './triage-list.html',
  styleUrl: './triage-list.css',
})
export class TriageListComponent {
  private readonly triageService = inject(TriageService);
  private readonly nodeService = inject(NodeService);

  readonly CATEGORIES = TRIAGE_CATEGORIES;
  readonly triages = signal<TriageResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.nodeService.selectedNode();
      this.load();
    });
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.triageService.getAll().subscribe({
      next: (data) => {
        this.triages.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar los triages. Verifique que el gateway esté activo.');
        this.triages.set([]);
        this.loading.set(false);
      },
    });
  }

  episodeNumber(admissionId: string): string {
    return `EP-${admissionId.replace(/-/g, '').substring(0, 8).toUpperCase()}`;
  }
}
