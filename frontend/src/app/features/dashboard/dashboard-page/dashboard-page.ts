import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { catchError, of, switchMap, tap } from 'rxjs';
import { ApiError } from '../../../core/models/api-error';
import { Dashboard } from '../../../core/models/dashboard';
import { STATUS_LABELS, TaskStatus } from '../../../core/models/task';
import { TaskService } from '../../../core/services/task-service';

interface StatusCard {
  status: TaskStatus;
  label: string;
  icon: string;
  count: number;
  percent: number;
}

const STATUS_ICONS: Record<TaskStatus, string> = {
  PENDENT: 'pending_actions',
  ON_GOING: 'autorenew',
  DONE: 'task_alt',
};

/** Ordem em que os cards aparecem. */
const STATUS_ORDER: TaskStatus[] = ['PENDENT', 'ON_GOING', 'DONE'];

@Component({
  selector: 'app-dashboard-page',
  imports: [DecimalPipe, RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressBarModule],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
})
export class DashboardPage {
  private readonly taskService = inject(TaskService);

  /** Cada incremento dispara uma nova busca (botão "Tentar de novo"). */
  private readonly reloads = signal(0);

  protected readonly dashboard = signal<Dashboard | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly cards = computed<StatusCard[]>(() => {
    const dashboard = this.dashboard();
    if (!dashboard) {
      return [];
    }
    return STATUS_ORDER.map((status) => {
      const count = dashboard.byStatus[status] ?? 0;
      return {
        status,
        label: STATUS_LABELS[status],
        icon: STATUS_ICONS[status],
        count,
        percent: dashboard.total > 0 ? (count / dashboard.total) * 100 : 0,
      };
    });
  });

  /** Percentual de tarefas concluídas, para a barra de progresso. */
  protected readonly donePercent = computed(() => this.cards().find((card) => card.status === 'DONE')?.percent ?? 0);

  constructor() {
    toObservable(this.reloads)
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.errorMessage.set(null);
        }),
        switchMap(() =>
          this.taskService.dashboard().pipe(
            catchError((error: ApiError) => {
              this.errorMessage.set(error.message);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((dashboard) => {
        this.loading.set(false);
        this.dashboard.set(dashboard);
      });
  }

  protected retry(): void {
    this.reloads.update((n) => n + 1);
  }
}
