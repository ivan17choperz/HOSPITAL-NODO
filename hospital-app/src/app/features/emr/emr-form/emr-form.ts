import { Component, computed, inject, signal } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdmissionService } from '../../../core/services/admission.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { EmrService } from '../../../core/services/emr.service';
import { FhirEmrService } from '../../../core/services/fhir-emr.service';
import { FhirService } from '../../../core/services/fhir.service';
import { AdmissionResponse } from '../../../core/models/admission.model';
import { DoctorResponse } from '../../../core/models/doctor.model';
import {
  EmrResponse,
  FREQUENCY_OPTIONS,
  MEDICATION_ROUTES,
} from '../../../core/models/emr.model';

@Component({
  selector: 'app-emr-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './emr-form.html',
  styleUrl: './emr-form.css',
})
export class EmrFormComponent {
  private readonly fb = inject(FormBuilder);
  private readonly admissionService = inject(AdmissionService);
  private readonly doctorService = inject(DoctorService);
  private readonly emrService = inject(EmrService);
  private readonly fhirBuilder = inject(FhirEmrService);
  private readonly fhirService = inject(FhirService);

  readonly ROUTES = MEDICATION_ROUTES;
  readonly FREQUENCIES = FREQUENCY_OPTIONS;

  readonly admissions = signal<AdmissionResponse[]>([]);
  readonly doctors = signal<DoctorResponse[]>([]);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<EmrResponse | null>(null);
  readonly fhirTab = signal<'condition' | 'medication' | 'bundle'>('bundle');
  readonly showFhir = signal(false);

  readonly form = this.fb.group({
    admissionId: ['', Validators.required],
    doctorId: ['', Validators.required],
    anamnesis: ['', Validators.required],
    clinicianNotes: [''],
    diagnoses: this.fb.array([this.newDiagnosis()]),
    medications: this.fb.array([this.newMedication()]),
  });

  get diagnosesArray(): FormArray {
    return this.form.get('diagnoses') as FormArray;
  }

  get medicationsArray(): FormArray {
    return this.form.get('medications') as FormArray;
  }

  diagnosisGroup(i: number): FormGroup {
    return this.diagnosesArray.at(i) as FormGroup;
  }

  medicationGroup(i: number): FormGroup {
    return this.medicationsArray.at(i) as FormGroup;
  }

  readonly selectedAdmission = computed(() => {
    const id = this.form.value.admissionId ?? '';
    return this.admissions().find((a) => a.id === id) ?? null;
  });

  readonly episodeNumber = computed(() => {
    const a = this.selectedAdmission();
    if (!a) return '—';
    return `EP-${a.id.replace(/-/g, '').substring(0, 8).toUpperCase()}`;
  });

  readonly fhirPreview = computed(() => {
    const a = this.selectedAdmission();
    const patientId = a?.patientId ?? 'patient-id';
    const admissionId = a?.id ?? 'admission-id';
    const diagnoses = (this.form.value.diagnoses ?? []).filter(
      (d: any) => d?.description || d?.icd10Code,
    );
    const medications = (this.form.value.medications ?? []).filter(
      (m: any) => m?.medicationName,
    );

    if (this.fhirTab() === 'condition') {
      if (!diagnoses.length) return '// Agregue al menos un diagnóstico';
      return JSON.stringify(
        this.fhirBuilder.buildCondition(diagnoses[0], patientId, admissionId),
        null,
        2,
      );
    }
    if (this.fhirTab() === 'medication') {
      if (!medications.length) return '// Agregue al menos un medicamento';
      return JSON.stringify(
        this.fhirBuilder.buildMedicationRequest(medications[0], patientId, admissionId),
        null,
        2,
      );
    }
    return JSON.stringify(
      this.fhirBuilder.buildBundle(diagnoses, medications, patientId, admissionId),
      null,
      2,
    );
  });

  constructor() {
    this.admissionService.getAll().subscribe({ next: (data) => this.admissions.set(data) });
    this.doctorService.getAll().subscribe({ next: (data) => this.doctors.set(data) });
  }

  private newDiagnosis(): FormGroup {
    return this.fb.group({
      icd10Code: [''],
      description: ['', Validators.required],
      clinicalStatus: ['active'],
      verificationStatus: ['confirmed'],
    });
  }

  private newMedication(): FormGroup {
    return this.fb.group({
      medicationName: ['', Validators.required],
      dose: [''],
      unit: ['mg'],
      route: ['oral'],
      frequency: ['bid'],
      durationDays: [null as number | null],
      notes: [''],
    });
  }

  addDiagnosis(): void {
    this.diagnosesArray.push(this.newDiagnosis());
  }

  removeDiagnosis(i: number): void {
    if (this.diagnosesArray.length > 1) this.diagnosesArray.removeAt(i);
  }

  addMedication(): void {
    this.medicationsArray.push(this.newMedication());
  }

  removeMedication(i: number): void {
    if (this.medicationsArray.length > 1) this.medicationsArray.removeAt(i);
  }

  isInvalid(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl.touched);
  }

  setFhirTab(tab: 'condition' | 'medication' | 'bundle'): void {
    this.fhirTab.set(tab);
  }

  toggleFhir(): void {
    this.showFhir.set(!this.showFhir());
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);

    const v = this.form.value;
    const a = this.selectedAdmission();

    const patientId = a?.patientId ?? '';
    const admissionId = v.admissionId!;
    const filteredDiagnoses = (v.diagnoses ?? []).filter((d: any) => d?.description);
    const filteredMedications = (v.medications ?? []).filter((m: any) => m?.medicationName);

    this.emrService
      .create({
        admissionId,
        patientId,
        anamnesis: v.anamnesis!,
        diagnoses: filteredDiagnoses as any,
        medications: filteredMedications as any,
        clinicianNotes: v.clinicianNotes ?? '',
        doctorId: v.doctorId ?? undefined,
      })
      .subscribe({
        next: (res) => {
          this.success.set(res);
          this.submitting.set(false);
          for (const d of filteredDiagnoses) {
            this.fhirService.registerCondition(
              this.fhirBuilder.buildCondition(d as any, patientId, admissionId)
            ).subscribe({ error: (e) => console.warn('FHIR condition failed:', e) });
          }
          for (const m of filteredMedications) {
            this.fhirService.registerMedicationRequest(
              this.fhirBuilder.buildMedicationRequest(m as any, patientId, admissionId)
            ).subscribe({ error: (e) => console.warn('FHIR medication failed:', e) });
          }
        },
        error: () => {
          this.error.set('No se pudo guardar el registro. Verifique que el gateway esté activo.');
          this.submitting.set(false);
        },
      });
  }

  reset(): void {
    this.form.reset();
    while (this.diagnosesArray.length > 1) this.diagnosesArray.removeAt(1);
    while (this.medicationsArray.length > 1) this.medicationsArray.removeAt(1);
    this.success.set(null);
    this.error.set(null);
  }
}
