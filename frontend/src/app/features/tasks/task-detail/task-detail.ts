import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterLink } from '@angular/router';
import { catchError, filter, of, switchMap, tap } from 'rxjs';
import { ApiError } from '../../../core/models/api-error';
import { PRIORITY_LABELS, STATUS_LABELS, Task } from '../../../core/models/task';
import { ConfirmService } from '../../../core/services/confirm-service';
import { NotificationService } from '../../../core/services/notification-service';
import { TaskService } from '../../../core/services/task-service';

@Component({
  selector: 'app-task-detail',
  imports: [DatePipe, LowerCasePipe, RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressBarModule],
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.scss',
})
export class TaskDetail {
  private readonly taskService = inject(TaskService);
  private readonly router = inject(Router);
  private readonly confirmService = inject(ConfirmService);
  private readonly notification = inject(NotificationService);

  /** UUID da rota /tasks/:id, entregue pelo withComponentInputBinding. */
  readonly id = input.required<string>();

  protected readonly priorityLabels = PRIORITY_LABELS;
  protected readonly statusLabels = STATUS_LABELS;

  protected readonly task = signal<Task | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly notFound = signal(false);

  constructor() {
    toObservable(this.id)
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.errorMessage.set(null);
          this.notFound.set(false);
        }),
        switchMap((id) =>
          this.taskService.getById(id).pipe(
            catchError((error: ApiError) => {
              this.notFound.set(error.code === 404);
              this.errorMessage.set(error.message);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((task) => {
        this.loading.set(false);
        this.task.set(task);
      });
  }

  protected toggleStatus(task: Task): void {
    const action = task.status === 'DONE' ? this.taskService.reopen(task.id) : this.taskService.complete(task.id);
    this.errorMessage.set(null);
    action.subscribe({
      next: (updated) => {
        this.task.set(updated);
        this.notification.success(updated.status === 'DONE' ? 'Tarefa concluída' : 'Tarefa reaberta');
      },
      error: (error: ApiError) => this.errorMessage.set(error.message),
    });
  }

  protected remove(task: Task): void {
    this.confirmService
      .confirmDelete(task.title)
      .pipe(
        filter(Boolean), // segue só se o usuário confirmou
        switchMap(() => this.taskService.delete(task.id)),
      )
      .subscribe({
        next: () => {
          this.notification.success('Tarefa excluída');
          this.router.navigate(['/tasks']);
        },
        error: (error: ApiError) => this.errorMessage.set(error.message),
      });
  }
}
