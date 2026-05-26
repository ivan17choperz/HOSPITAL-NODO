import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AdmissionService } from '../../../core/services/admission.service';
import { AdmissionResponse } from '../../../core/models/admission.model';

@Component({
  selector: 'app-admission-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admission-form.html',
  styleUrl: './admission-form.css',
})
export class AdmissionFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly admissionService = inject(AdmissionService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<AdmissionResponse | null>(null);

  readonly form = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    gender: [''],
    birthDate: [''],
    identifier: [''],
    ward: ['', Validators.required],
  });

  isInvalid(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl.touched);
  }

  episodeNumber(id: string): string {
    return `EP-${id.replace(/-/g, '').substring(0, 8).toUpperCase()}`;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    const v = this.form.value;
    this.admissionService
      .create({
        patientId: crypto.randomUUID(),
        firstName: v.firstName!,
        lastName: v.lastName!,
        gender: v.gender ?? '',
        birthDate: v.birthDate ?? '',
        identifier: v.identifier ?? '',
        ward: v.ward!,
      })
      .subscribe({
        next: (res) => {
          this.success.set(res);
          this.submitting.set(false);
        },
        error: () => {
          this.error.set('Error al registrar la admisión. Verifique los datos e intente de nuevo.');
          this.submitting.set(false);
        },
      });
  }

  reset(): void {
    this.form.reset();
    this.success.set(null);
    this.error.set(null);
  }

  goToList(): void {
    this.router.navigate(['/admissions']);
  }
}
