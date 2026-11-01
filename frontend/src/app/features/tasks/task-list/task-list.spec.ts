import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { apiErrorInterceptor } from '../../../core/interceptors/api-error-interceptor';
import { ApiResponse } from '../../../core/models/api-response';
import { Page } from '../../../core/models/page';
import { Task } from '../../../core/models/task';
import { of } from 'rxjs';
import { ConfirmService } from '../../../core/services/confirm-service';
import { NotificationService } from '../../../core/services/notification-service';
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

  it('deve já abrir filtrada pelo status que vem da URL (?status=DONE)', async () => {
    // Descarta a busca do componente criado no beforeEach.
    http.expectOne((r) => r.url.endsWith('/tasks')).flush(pageOf([]));

    const filtered = TestBed.createComponent(TaskList);
    filtered.componentRef.setInput('status', 'DONE');
    await filtered.whenStable();

    const req = http.expectOne((r) => r.url.endsWith('/tasks'));
    expect(req.request.params.get('status')).toBe('DONE');
    req.flush(pageOf([]));
  });

  it('deve ignorar um status inválido na URL', async () => {
    http.expectOne((r) => r.url.endsWith('/tasks')).flush(pageOf([]));

    const filtered = TestBed.createComponent(TaskList);
    filtered.componentRef.setInput('status', 'QUALQUER');
    await filtered.whenStable();

    const req = http.expectOne((r) => r.url.endsWith('/tasks'));
    expect(req.request.params.has('status')).toBe(false);
    req.flush(pageOf([]));
  });

  it('deve excluir só depois da confirmação no diálogo e avisar com o snackbar', async () => {
    http.expectOne((r) => r.url.endsWith('/tasks')).flush(pageOf([task('1', 'Estudar Angular')]));
    await fixture.whenStable();

    const confirm = vi.spyOn(TestBed.inject(ConfirmService), 'confirmDelete').mockReturnValue(of(true));
    const success = vi.spyOn(TestBed.inject(NotificationService), 'success').mockImplementation(() => undefined);

    element.querySelector<HTMLButtonElement>('button[aria-label="Excluir"]')!.click();

    expect(confirm).toHaveBeenCalledWith('Estudar Angular');
    http.expectOne((r) => r.method === 'DELETE' && r.url.endsWith('/tasks/1')).flush({
      status: 'OK', code: 200, message: 'Tarefa excluída', payload: null, timestamp: '', error: false,
    });
    await fixture.whenStable();

    expect(success).toHaveBeenCalledWith('Tarefa excluída');
    // Depois de excluir, a lista é buscada de novo.
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith('/tasks')).flush(pageOf([]));
  });

  it('não deve excluir quando o usuário cancela o diálogo', async () => {
    http.expectOne((r) => r.url.endsWith('/tasks')).flush(pageOf([task('1', 'Estudar Angular')]));
    await fixture.whenStable();

    vi.spyOn(TestBed.inject(ConfirmService), 'confirmDelete').mockReturnValue(of(false));

    element.querySelector<HTMLButtonElement>('button[aria-label="Excluir"]')!.click();

    http.expectNone((r) => r.method === 'DELETE');
  });
});
