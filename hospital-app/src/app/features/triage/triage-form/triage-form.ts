import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdmissionService } from '../../../core/services/admission.service';
import { ManchesterClassifierService } from '../../../core/services/manchester-classifier.service';
import { TriageService } from '../../../core/services/triage.service';
import { FhirService } from '../../../core/services/fhir.service';
import { AdmissionResponse } from '../../../core/models/admission.model';
import {
  TRIAGE_CATEGORIES,
  TriageCategory,
  TriageResponse,
  VitalSigns,
} from '../../../core/models/triage.model';

@Component({
  selector: 'app-triage-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './triage-form.html',
  styleUrl: './triage-form.css',
})
export class TriageFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly admissionService = inject(AdmissionService);
  private readonly classifier = inject(ManchesterClassifierService);
  private readonly triageService = inject(TriageService);
  private readonly fhirService = inject(FhirService);

  readonly CATEGORIES = TRIAGE_CATEGORIES;
  readonly CATEGORY_KEYS: TriageCategory[] = ['RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE'];
  readonly admissions = signal<AdmissionResponse[]>([]);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<TriageResponse | null>(null);
  readonly showFhir = signal(false);

  readonly form = this.fb.group({
    admissionId: ['', Validators.required],
    temperature: [null as number | null],
    heartRate: [null as number | null],
    systolicBp: [null as number | null],
    diastolicBp: [null as number | null],
    oxygenSaturation: [null as number | null],
    respiratoryRate: [null as number | null],
    painScore: [null as number | null],
    notes: [''],
  });

  readonly vitals = computed<VitalSigns>(() => {
    const v = this.form.value;
    return {
      temperature: v.temperature ?? null,
      heartRate: v.heartRate ?? null,
      systolicBp: v.systolicBp ?? null,
      diastolicBp: v.diastolicBp ?? null,
      oxygenSaturation: v.oxygenSaturation ?? null,
      respiratoryRate: v.respiratoryRate ?? null,
      painScore: v.painScore ?? null,
    };
  });

  readonly category = computed<TriageCategory>(() => this.classifier.classify(this.vitals()));

  readonly categoryInfo = computed(() => this.CATEGORIES[this.category()]);

  readonly fhirPreview = computed(() => {
    const admissionId = this.form.value.admissionId ?? '';
    const selectedAdmission = this.admissions().find((a) => a.id === admissionId);
    const patientId = selectedAdmission?.patientId ?? 'unknown';
    const obs = this.classifier.buildFhirObservation(
      crypto.randomUUID(),
      patientId,
      admissionId,
      this.vitals(),
      this.category(),
    );
    return JSON.stringify(obs, null, 2);
  });

  readonly episodeNumber = computed(() => {
    const id = this.form.value.admissionId ?? '';
    if (!id) return '—';
    return `EP-${id.replace(/-/g, '').substring(0, 8).toUpperCase()}`;
  });

  readonly hasAnyVital = computed(() => {
    const v = this.vitals();
    return Object.values(v).some((val) => val !== null);
  });

  constructor() {
    this.admissionService.getAll().subscribe({
      next: (data) => this.admissions.set(data),
    });

    // recompute vitals on form value changes
    effect(() => {
      this.form.valueChanges.subscribe(() => {
        // trigger computed signals by reading them
        this.vitals();
      });
    });
  }

  isInvalid(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl.touched);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.hasAnyVital()) {
      this.error.set('Ingrese al menos un signo vital antes de registrar el triage.');
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    const v = this.form.value;
    const admissionId = v.admissionId!;
    const selectedAdmission = this.admissions().find((a) => a.id === admissionId);

    this.triageService
      .create({
        admissionId,
        patientId: selectedAdmission?.patientId ?? '',
        vitalSigns: this.vitals(),
        category: this.category(),
        notes: v.notes ?? '',
      })
      .subscribe({
        next: (res) => {
          this.success.set(res);
          this.submitting.set(false);
          this.fhirService.registerTriage({
            patientId: selectedAdmission?.patientId ?? '',
            episodeNumber: admissionId,
            heartRate: v.heartRate ?? null,
            respiratoryRate: v.respiratoryRate ?? null,
            oxygenSaturation: v.oxygenSaturation ?? null,
            temperatureCelsius: v.temperature ?? null,
            systolicBloodPressure: v.systolicBp ?? null,
            diastolicBloodPressure: v.diastolicBp ?? null,
            painScale0To10: v.painScore ?? null,
            triageScale: 'MANCHESTER',
          }).subscribe({ error: (e) => console.warn('FHIR triage registration failed:', e) });
        },
        error: () => {
          this.error.set(
            'No se pudo registrar el triage. Verifique que el gateway esté activo.',
          );
          this.submitting.set(false);
        },
      });
  }

  reset(): void {
    this.form.reset();
    this.success.set(null);
    this.error.set(null);
    this.showFhir.set(false);
  }

  toggleFhir(): void {
    this.showFhir.update((v) => !v);
  }
}
