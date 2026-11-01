import { BreakpointObserver } from '@angular/cdk/layout';
import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { catchError, debounceTime, filter, map, of, switchMap, tap } from 'rxjs';
import { ApiError } from '../../../core/models/api-error';
import { Page } from '../../../core/models/page';
import { Priority, PRIORITY_LABELS, STATUS_LABELS, Task, TaskStatus } from '../../../core/models/task';
import { TaskQuery, TaskSortField } from '../../../core/models/task-query';
import { ConfirmService } from '../../../core/services/confirm-service';
import { NotificationService } from '../../../core/services/notification-service';
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
    MatMenuModule,
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
export class TaskList implements OnInit {
  private readonly taskService = inject(TaskService);
  private readonly confirmService = inject(ConfirmService);
  private readonly notification = inject(NotificationService);

  /** true em telas de até 600px: a tabela mostra menos colunas e as ações viram um menu. */
  protected readonly isHandset = toSignal(
    inject(BreakpointObserver)
      .observe('(max-width: 600px)')
      .pipe(map((state) => state.matches)),
    { initialValue: false },
  );

  /** Filtro de status vindo da URL (/tasks?status=DONE), usado pelos cards do dashboard. */
  readonly status = input<string>();

  protected readonly columns = computed(() =>
    this.isHandset()
      ? ['title', 'status', 'actions']
      : ['title', 'priority', 'status', 'dueDate', 'createdAt', 'actions'],
  );
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

  ngOnInit(): void {
    // Inputs só têm valor a partir do ngOnInit; aqui ainda dá tempo de ajustar a primeira busca.
    const status = this.status() as TaskStatus | undefined;
    if (status && status in STATUS_LABELS) {
      this.filters.patchValue({ status }, { emitEvent: false });
      this.query.update((q) => ({ ...q, status }));
    }
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
      next: (updated) => {
        this.notification.success(updated.status === 'DONE' ? 'Tarefa concluída' : 'Tarefa reaberta');
        this.reload();
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
