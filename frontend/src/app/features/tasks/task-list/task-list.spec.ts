import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { apiErrorInterceptor } from '../../../core/interceptors/api-error-interceptor';
import { ApiResponse } from '../../../core/models/api-response';
import { Page } from '../../../core/models/page';
import { Task } from '../../../core/models/task';
import { TaskList } from './task-list';

function task(id: string, title: string, overrides: Partial<Task> = {}): Task {
  return {
    id,
    title,
    description: null,
    priority: 'HIGH',
    status: 'PENDENT',
    dueDate: '2026-12-31',
    createdAt: '2026-10-31T10:00:00Z',
    updatedAt: '2026-10-31T10:00:00Z',
    ...overrides,
  };
}

function pageOf(items: Task[]): ApiResponse<Page<Task>> {
  return {
    status: 'OK',
    code: 200,
    message: 'Tarefas listadas com sucesso',
    payload: { items, page: 0, size: 10, totalItems: items.length, totalPages: items.length ? 1 : 0 },
    timestamp: '2026-10-31T10:00:00Z',
    error: false,
  };
}

describe('TaskList', () => {
  let fixture: ComponentFixture<TaskList>;
  let http: HttpTestingController;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskList],
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([apiErrorInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskList);
    http = TestBed.inject(HttpTestingController);
    element = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('deve buscar a primeira página, das mais recentes para as mais antigas', async () => {
    const req = http.expectOne((r) => r.url.endsWith('/tasks'));
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('CREATED_AT');
    expect(req.request.params.get('direction')).toBe('DESC');

    req.flush(pageOf([task('1', 'Estudar Angular'), task('2', 'Revisar PR', { status: 'DONE', priority: 'LOW' })]));
    await fixture.whenStable();

    expect(element.querySelectorAll('tr.mat-mdc-row').length).toBe(2);
    expect(element.textContent).toContain('Estudar Angular');
    expect(element.textContent).toContain('Alta');
    expect(element.textContent).toContain('Concluída');
  });

  it('deve avisar quando nenhuma tarefa é encontrada', async () => {
    http.expectOne((r) => r.url.endsWith('/tasks')).flush(pageOf([]));
    await fixture.whenStable();

    expect(element.textContent).toContain('Nenhuma tarefa encontrada.');
  });

  it('deve mostrar a mensagem de erro que vem da API', async () => {
    http.expectOne((r) => r.url.endsWith('/tasks')).flush(
      { status: 'BAD_REQUEST', code: 400, message: 'Requisição inválida', payload: null, timestamp: '', error: true },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(element.querySelector('[role="alert"]')?.textContent).toContain('Requisição inválida');
  });
});
