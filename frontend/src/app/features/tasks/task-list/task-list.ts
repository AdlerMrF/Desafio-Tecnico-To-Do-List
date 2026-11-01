import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { catchError, debounceTime, of, switchMap, tap } from 'rxjs';
import { ApiError } from '../../../core/models/api-error';
import { Page } from '../../../core/models/page';
import { Priority, PRIORITY_LABELS, STATUS_LABELS, Task, TaskStatus } from '../../../core/models/task';
import { TaskQuery, TaskSortField } from '../../../core/models/task-query';
import { TaskService } from '../../../core/services/task-service';

/** Colunas da tabela que podem ser ordenadas, e o campo correspondente na API. */
const SORTABLE_COLUMNS: Record<string, TaskSortField> = {
  title: 'TITLE',
  dueDate: 'DUE_DATE',
  createdAt: 'CREATED_AT',
};

const DEFAULT_QUERY: TaskQuery = { page: 0, size: 10, sort: 'CREATED_AT', direction: 'DESC' };

@Component({
  selector: 'app-task-list',
  imports: [
    DatePipe,
    LowerCasePipe,
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSortModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './task-list.html',
  styleUrl: './task-list.scss',
})
export class TaskList {
  private readonly taskService = inject(TaskService);

  protected readonly columns = ['title', 'priority', 'status', 'dueDate', 'createdAt', 'actions'];
  // Record<string, string>: a linha da tabela (let task) chega ao template sem tipo.
  protected readonly priorityLabels: Record<string, string> = PRIORITY_LABELS;
  protected readonly statusLabels: Record<string, string> = STATUS_LABELS;
  protected readonly priorities = Object.keys(PRIORITY_LABELS) as Priority[];
  protected readonly statuses = Object.keys(STATUS_LABELS) as TaskStatus[];
  protected readonly pageSizeOptions = [5, 10, 25, 50];

  protected readonly filters = new FormGroup({
    title: new FormControl('', { nonNullable: true }),
    status: new FormControl<TaskStatus | ''>('', { nonNullable: true }),
    priority: new FormControl<Priority | ''>('', { nonNullable: true }),
  });

  /** Tudo o que define a busca. Cada mudança aqui dispara uma nova chamada à API. */
  protected readonly query = signal<TaskQuery>(DEFAULT_QUERY);

  protected readonly tasks = signal<Task[]>([]);
  protected readonly totalItems = signal(0);
  protected readonly loading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.filters.valueChanges.pipe(debounceTime(400), takeUntilDestroyed()).subscribe((value) => {
      this.query.update((q) => ({
        ...q,
        title: value.title?.trim() || undefined,
        status: value.status || undefined,
        priority: value.priority || undefined,
        page: 0,
      }));
    });

    toObservable(this.query)
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.errorMessage.set(null);
        }),
        // switchMap cancela a busca anterior se o usuário mudar o filtro antes da resposta chegar.
        switchMap((query) =>
          this.taskService.list(query).pipe(
            catchError((error: ApiError) => {
              this.errorMessage.set(error.message);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((page: Page<Task> | null) => {
        this.loading.set(false);
        if (page) {
          this.tasks.set(page.items);
          this.totalItems.set(page.totalItems);
        }
      });
  }

  protected onPage(event: PageEvent): void {
    this.query.update((q) => ({ ...q, page: event.pageIndex, size: event.pageSize }));
  }

  protected onSort(sort: Sort): void {
    const field = SORTABLE_COLUMNS[sort.active];
    this.query.update((q) =>
      field && sort.direction
        ? { ...q, sort: field, direction: sort.direction === 'asc' ? 'ASC' : 'DESC', page: 0 }
        : { ...q, sort: DEFAULT_QUERY.sort, direction: DEFAULT_QUERY.direction, page: 0 },
    );
  }

  protected clearFilters(): void {
    this.filters.reset();
  }

  protected toggleStatus(task: Task): void {
    const action = task.status === 'DONE' ? this.taskService.reopen(task.id) : this.taskService.complete(task.id);
    action.subscribe({
      next: () => this.reload(),
      error: (error: ApiError) => this.errorMessage.set(error.message),
    });
  }

  protected remove(task: Task): void {
    // Provisório: o cartão 23 troca o confirm do navegador por um MatDialog.
    if (!confirm(`Excluir a tarefa "${task.title}"?`)) {
      return;
    }
    this.taskService.delete(task.id).subscribe({
      next: () => {
        // Se era o último item da página, volta uma página para não mostrar uma página vazia.
        const q = this.query();
        const lastOnPage = this.tasks().length === 1 && (q.page ?? 0) > 0;
        this.query.set({ ...q, page: lastOnPage ? (q.page ?? 1) - 1 : q.page });
      },
      error: (error: ApiError) => this.errorMessage.set(error.message),
    });
  }

  private reload(): void {
    this.query.update((q) => ({ ...q }));
  }
}
