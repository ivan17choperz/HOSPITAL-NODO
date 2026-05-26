import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { interval, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReportsService } from '../../../core/services/reports.service';
import { NodeService } from '../../../core/services/node.service';
import { ReportSnapshot } from '../../../core/models/reports.model';

const REFRESH_SECONDS = 30;
const DONUT_R = 54;
const DONUT_C = 2 * Math.PI * DONUT_R;

@Component({
  selector: 'app-reports-dashboard',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './reports-dashboard.html',
  styleUrl: './reports-dashboard.css',
})
export class ReportsDashboardComponent implements OnInit {
  private readonly reportsService = inject(ReportsService);
  private readonly nodeService = inject(NodeService);
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);

  readonly snapshot = signal<ReportSnapshot | null>(null);
  readonly loading = signal(false);
  readonly countdown = signal(REFRESH_SECONDS);
  readonly peerStatuses = signal<Record<string, string>>({});

  readonly peerEntries = computed(() => Object.entries(this.peerStatuses()));

  readonly nodeBarMax = computed(() => {
    const s = this.snapshot();
    if (!s) return 1;
    return Math.max(1, ...s.nodeReports.flatMap(n => [n.admissions, n.triages, n.emr]));
  });

  readonly donutSegments = computed(() => {
    const s = this.snapshot();
    if (!s || s.totalTriages === 0) return [];
    let accumulated = 0;
    return s.triageStats
      .filter(t => t.count > 0)
      .map(t => {
        const segLen = (t.count / s.totalTriages) * DONUT_C;
        const seg = {
          dashArray: `${segLen.toFixed(2)} ${(DONUT_C - segLen).toFixed(2)}`,
          dashOffset: `${(DONUT_C - accumulated).toFixed(2)}`,
          color: t.color,
          label: t.label,
          count: t.count,
          pct: Math.round((t.count / s.totalTriages) * 100),
        };
        accumulated += segLen;
        return seg;
      });
  });

  readonly wardBarMax = computed(() => {
    const s = this.snapshot();
    return s && s.wardStats.length > 0 ? s.wardStats[0].count : 1;
  });

  ngOnInit(): void {
    this.load();
    interval(1000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        const c = this.countdown();
        if (c <= 1) {
          this.countdown.set(REFRESH_SECONDS);
          this.load();
        } else {
          this.countdown.set(c - 1);
        }
      });
  }

  load(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.reportsService
      .fetchSnapshot()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: snap => {
          this.snapshot.set(snap);
          this.loading.set(false);
          this.countdown.set(REFRESH_SECONDS);
        },
        error: () => this.loading.set(false),
      });
    const gatewayUrl = this.nodeService.selectedNode().gatewayUrl;
    this.http.get<Record<string, string>>(`${gatewayUrl}/api/sync/status/peers`)
      .pipe(takeUntilDestroyed(this.destroyRef), catchError(() => of({})))
      .subscribe(s => this.peerStatuses.set(s));
  }

  barPct(value: number): number {
    return Math.round((value / this.nodeBarMax()) * 100);
  }

  wardPct(count: number): number {
    return Math.round((count / this.wardBarMax()) * 100);
  }

  formatTime(date: Date): string {
    return date.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  }
}
