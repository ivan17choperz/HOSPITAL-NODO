import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { EmrService } from '../../../core/services/emr.service';
import { NodeService } from '../../../core/services/node.service';
import { EmrResponse } from '../../../core/models/emr.model';

@Component({
  selector: 'app-emr-list',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './emr-list.html',
  styleUrl: './emr-list.css',
})
export class EmrListComponent {
  private readonly emrService = inject(EmrService);
  private readonly nodeService = inject(NodeService);

  readonly records = signal<EmrResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly expanded = signal<string | null>(null);

  constructor() {
    effect(() => {
      this.nodeService.selectedNode();
      this.load();
    });
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.emrService.getAll().subscribe({
      next: (data) => {
        this.records.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la Historia Clínica. Verifique que el gateway esté activo.');
        this.records.set([]);
        this.loading.set(false);
      },
    });
  }

  episodeNumber(admissionId: string): string {
    return `EP-${admissionId.replace(/-/g, '').substring(0, 8).toUpperCase()}`;
  }

  toggle(id: string): void {
    this.expanded.update((v) => (v === id ? null : id));
  }
}
