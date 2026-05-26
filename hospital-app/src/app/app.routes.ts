import { Routes } from '@angular/router';
import { AdmissionListComponent } from './features/admission/admission-list/admission-list';
import { AdmissionFormComponent } from './features/admission/admission-form/admission-form';
import { TriageListComponent } from './features/triage/triage-list/triage-list';
import { TriageFormComponent } from './features/triage/triage-form/triage-form';
import { EmrListComponent } from './features/emr/emr-list/emr-list';
import { EmrFormComponent } from './features/emr/emr-form/emr-form';
import { HcViewComponent } from './features/hc/hc-view/hc-view';
import { ReportsDashboardComponent } from './features/reports/reports-dashboard/reports-dashboard';

export const routes: Routes = [
  { path: '', redirectTo: 'admissions', pathMatch: 'full' },
  { path: 'admissions', component: AdmissionListComponent },
  { path: 'admissions/new', component: AdmissionFormComponent },
  { path: 'triage', component: TriageListComponent },
  { path: 'triage/new', component: TriageFormComponent },
  { path: 'emr', component: EmrListComponent },
  { path: 'emr/new', component: EmrFormComponent },
  { path: 'hc', component: HcViewComponent },
  { path: 'reports', component: ReportsDashboardComponent },
];
