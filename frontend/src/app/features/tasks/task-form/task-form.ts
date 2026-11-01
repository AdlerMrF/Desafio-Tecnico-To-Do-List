import { Component, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { DateAdapter, provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { EMPTY, catchError, of, switchMap, tap } from 'rxjs';
import { PtBrDateAdapter } from '../../../core/i18n/pt-br-date-adapter';
import { ApiError } from '../../../core/models/api-error';
import { PRIORITY_LABELS, Priority, STATUS_LABELS, Task, TaskRequest, TaskStatus } from '../../../core/models/task';
import { TaskService } from '../../../core/services/task-service';

/** Mesmo limite do @Size(max = 150) do backend. */
export const TITLE_MAX_LENGTH = 150;

/** Equivale ao @NotBlank do backend: "   " não é um título válido. */
function notBlank(control: AbstractControl<string>): ValidationErrors | null {
  return control.value.trim().length === 0 ? { required: true } : null;
}

/** Converte a data do datepicker em "yyyy-MM-dd" sem passar por UTC (evita perder um dia). */
function toIsoDate(date: Date | null): string | null {
  if (!date) {
    return null;
  }
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

/** Converte "yyyy-MM-dd" vindo da API em Date local, também sem passar por UTC. */
function fromIsoDate(value: string | null): Date | null {
  if (!value) {
    return null;
  }
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

@Component({
  selector: 'app-task-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
  ],
  providers: [provideNativeDateAdapter(), { provide: DateAdapter, useClass: PtBrDateAdapter }],
  templateUrl: './task-form.html',
  styleUrl: './task-form.scss',
})
export class TaskForm {
  private readonly taskService = inject(TaskService);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);

  /** UUID da rota /tasks/:id/edit. Em /tasks/new não existe, e o formulário fica no modo cadastro. */
  readonly id = input<string>();

  protected readonly isEdit = computed(() => !!this.id());
  protected readonly titleMaxLength = TITLE_MAX_LENGTH;

  protected readonly priorities = Object.entries(PRIORITY_LABELS) as [Priority, string][];
  protected readonly statuses = Object.entries(STATUS_LABELS) as [TaskStatus, string][];

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly loadFailed = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly form = this.fb.group({
    title: ['', [Validators.required, notBlank, Validators.maxLength(TITLE_MAX_LENGTH)]],
    description: [''],
    priority: this.fb.control<Priority>('MEDIUM', Validators.required),
    status: this.fb.control<TaskStatus>('PENDENT', Validators.required),
    dueDate: this.fb.control<Date | null>(null),
  });

  constructor() {
    // No modo edição, carrega a tarefa e preenche o formulário.
    toObservable(this.id)
      .pipe(
        switchMap((id) => {
          if (!id) {
            return EMPTY;
          }
          this.loading.set(true);
          this.loadFailed.set(false);
          this.errorMessage.set(null);
          return this.taskService.getById(id).pipe(
            catchError((error: ApiError) => {
              this.loadFailed.set(true);
              this.errorMessage.set(error.message);
              return of(null);
            }),
            tap(() => this.loading.set(false)),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((task) => {
        if (task) {
          this.fill(task);
        }
      });
  }

  protected submit(): void {
    if (this.form.invalid) {
      // Mostra os erros de todos os campos, inclusive os que o usuário não tocou.
      this.form.markAllAsTouched();
      return;
    }

    const id = this.id();
    const request = this.toRequest();
    const save = id ? this.taskService.update(id, request) : this.taskService.create(request);

    this.saving.set(true);
    this.errorMessage.set(null);
    save.subscribe({
      next: (task) => this.router.navigate(['/tasks', task.id]),
      error: (error: ApiError) => {
        this.saving.set(false);
        this.errorMessage.set(error.message);
        this.applyServerErrors(error);
      },
    });
  }

  /** Mensagem do campo: primeiro a do backend (server), depois as validações locais. */
  protected errorFor(field: 'title' | 'priority' | 'status' | 'dueDate'): string | null {
    const errors = this.form.controls[field].errors;
    if (!errors) {
      return null;
    }
    if (errors['server']) {
      return errors['server'] as string;
    }
    if (errors['required']) {
      return field === 'title' ? 'O título é obrigatório' : 'Campo obrigatório';
    }
    if (errors['maxlength']) {
      return `O título deve ter no máximo ${TITLE_MAX_LENGTH} caracteres`;
    }
    if (errors['matDatepickerParse']) {
      return 'Data inválida. Use o formato dd/mm/aaaa';
    }
    return 'Valor inválido';
  }

  private fill(task: Task): void {
    this.form.setValue({
      title: task.title,
      description: task.description ?? '',
      priority: task.priority,
      status: task.status,
      dueDate: fromIsoDate(task.dueDate),
    });
  }

  /** Monta o corpo da requisição. Nunca envia id: quem gera o UUID é o backend. */
  private toRequest(): TaskRequest {
    const value = this.form.getRawValue();
    const description = value.description.trim();
    return {
      title: value.title.trim(),
      description: description.length > 0 ? description : null,
      priority: value.priority,
      status: value.status,
      dueDate: toIsoDate(value.dueDate),
    };
  }

  /** Coloca no campo certo cada mensagem do 400 (payload = { campo: mensagem }). */
  private applyServerErrors(error: ApiError): void {
    if (error.code !== 400 || !error.details) {
      return;
    }
    for (const [field, message] of Object.entries(error.details)) {
      const control = this.form.get(field);
      if (control) {
        control.setErrors({ server: message });
        control.markAsTouched();
      }
    }
  }
}
