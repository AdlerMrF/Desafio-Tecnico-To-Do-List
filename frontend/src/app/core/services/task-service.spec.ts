import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { apiErrorInterceptor } from '../interceptors/api-error-interceptor';
import { ApiError } from '../models/api-error';
import { ApiResponse } from '../models/api-response';
import { Page } from '../models/page';
import { Task } from '../models/task';
import { TaskService } from './task-service';

const TASKS_URL = `${environment.apiUrl}/tasks`;

const TASK: Task = {
  id: '3fa85f64-5717-4562-b3fc-2c963f66afa6',
  title: 'Estudar Angular',
  description: null,
  priority: 'HIGH',
  status: 'PENDENT',
  dueDate: '2026-12-31',
  createdAt: '2026-10-31T10:00:00Z',
  updatedAt: '2026-10-31T10:00:00Z',
};

function ok<T>(payload: T, code = 200, status = 'OK'): ApiResponse<T> {
  return { status, code, message: 'success', payload, timestamp: '2026-10-31T10:00:00Z', error: false };
}

function fail(code: number, status: string, message: string, payload: unknown = null): ApiResponse<unknown> {
  return { status, code, message, payload, timestamp: '2026-10-31T10:00:00Z', error: true };
}

describe('TaskService', () => {
  let service: TaskService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([apiErrorInterceptor])), provideHttpClientTesting()],
    });
    service = TestBed.inject(TaskService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('deve listar enviando só os filtros preenchidos e devolver o payload', () => {
    const page: Page<Task> = { items: [TASK], page: 0, size: 10, totalItems: 1, totalPages: 1 };
    let result: Page<Task> | undefined;

    service.list({ title: 'estudar', status: 'PENDENT', page: 0, priority: undefined }).subscribe((p) => (result = p));

    const req = http.expectOne((r) => r.url === TASKS_URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('title')).toBe('estudar');
    expect(req.request.params.get('status')).toBe('PENDENT');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.has('priority')).toBe(false);
    req.flush(ok(page));

    expect(result).toEqual(page);
  });

  it('deve cadastrar com POST sem enviar id e devolver a tarefa criada', () => {
    let result: Task | undefined;

    service.create({ title: 'Estudar Angular', priority: 'HIGH' }).subscribe((t) => (result = t));

    const req = http.expectOne(TASKS_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'Estudar Angular', priority: 'HIGH' });
    req.flush(ok(TASK, 201, 'CREATED'));

    expect(result?.id).toBe(TASK.id);
  });

  it('deve chamar as rotas certas para concluir, reabrir e excluir', () => {
    service.complete(TASK.id).subscribe();
    const complete = http.expectOne(`${TASKS_URL}/${TASK.id}/complete`);
    expect(complete.request.method).toBe('PATCH');
    complete.flush(ok({ ...TASK, status: 'DONE' }));

    service.reopen(TASK.id).subscribe();
    const reopen = http.expectOne(`${TASKS_URL}/${TASK.id}/reopen`);
    expect(reopen.request.method).toBe('PATCH');
    reopen.flush(ok(TASK));

    let deleted = false;
    service.delete(TASK.id).subscribe(() => (deleted = true));
    const del = http.expectOne(`${TASKS_URL}/${TASK.id}`);
    expect(del.request.method).toBe('DELETE');
    del.flush(ok(null));
    expect(deleted).toBe(true);
  });

  it('deve converter o 404 da API em ApiError com a mensagem do contrato', () => {
    let error: unknown;

    service.getById(TASK.id).subscribe({ error: (e) => (error = e) });

    http
      .expectOne(`${TASKS_URL}/${TASK.id}`)
      .flush(fail(404, 'NOT_FOUND', `Tarefa não encontrada: ${TASK.id}`), { status: 404, statusText: 'Not Found' });

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).code).toBe(404);
    expect((error as ApiError).message).toBe(`Tarefa não encontrada: ${TASK.id}`);
  });

  it('deve trazer os campos inválidos do erro de validação em details', () => {
    let error: ApiError | undefined;

    service.create({ title: '', priority: 'HIGH' }).subscribe({ error: (e) => (error = e) });

    http
      .expectOne(TASKS_URL)
      .flush(fail(400, 'BAD_REQUEST', 'Dados inválidos', { title: 'O título é obrigatório' }), {
        status: 400,
        statusText: 'Bad Request',
      });

    expect(error?.code).toBe(400);
    expect(error?.details).toEqual({ title: 'O título é obrigatório' });
  });

  it('deve avisar quando o servidor não responde', () => {
    let error: ApiError | undefined;

    service.dashboard().subscribe({ error: (e) => (error = e) });

    http.expectOne(`${environment.apiUrl}/dashboard`).error(new ProgressEvent('error'));

    expect(error?.code).toBe(0);
    expect(error?.message).toContain('Não foi possível conectar ao servidor');
  });
});
