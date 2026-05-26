import { Component, effect, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, LowerCasePipe } from '@angular/common';
import { AdmissionService } from '../../../core/services/admission.service';
import { NodeService } from '../../../core/services/node.service';
import { AdmissionResponse } from '../../../core/models/admission.model';

@Component({
  selector: 'app-admission-list',
  standalone: true,
  imports: [RouterLink, DatePipe, LowerCasePipe],
  templateUrl: './admission-list.html',
  styleUrl: './admission-list.css',
})
export class AdmissionListComponent {
  private readonly admissionService = inject(AdmissionService);
  private readonly nodeService = inject(NodeService);

  readonly admissions = signal<AdmissionResponse[]>([]);
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
    this.admissionService.getAll().subscribe({
      next: (data) => {
        this.admissions.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo conectar al servidor. Verifique que el gateway esté activo.');
        this.admissions.set([]);
        this.loading.set(false);
      },
    });
  }

  episodeNumber(id: string): string {
    return `EP-${id.replace(/-/g, '').substring(0, 8).toUpperCase()}`;
  }
}
