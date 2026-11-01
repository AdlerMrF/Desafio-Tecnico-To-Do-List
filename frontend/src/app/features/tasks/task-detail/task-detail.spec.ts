import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { apiErrorInterceptor } from '../../../core/interceptors/api-error-interceptor';
import { ApiResponse } from '../../../core/models/api-response';
import { Task } from '../../../core/models/task';
import { TaskDetail } from './task-detail';

const ID = '3fa85f64-5717-4562-b3fc-2c963f66afa6';

const TASK: Task = {
  id: ID,
  title: 'Estudar Angular',
  description: null,
  priority: 'HIGH',
  status: 'PENDENT',
  dueDate: '2026-12-31',
  createdAt: '2026-10-31T10:00:00Z',
  updatedAt: '2026-10-31T10:00:00Z',
};

function ok(payload: Task): ApiResponse<Task> {
  return { status: 'OK', code: 200, message: 'Tarefa encontrada', payload, timestamp: '', error: false };
}

describe('TaskDetail', () => {
  let fixture: ComponentFixture<TaskDetail>;
  let http: HttpTestingController;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskDetail],
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([apiErrorInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskDetail);
    fixture.componentRef.setInput('id', ID);
    http = TestBed.inject(HttpTestingController);
    element = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('deve buscar a tarefa pelo UUID da rota e mostrar os dados', async () => {
    http.expectOne((r) => r.url.endsWith(`/tasks/${ID}`)).flush(ok(TASK));
    await fixture.whenStable();

    expect(element.textContent).toContain('Estudar Angular');
    expect(element.textContent).toContain('Prioridade alta');
    expect(element.textContent).toContain('Pendente');
    expect(element.textContent).toContain('31/12/2026');
    expect(element.textContent).toContain('Sem descrição.');
  });

  it('deve mostrar "Tarefa não encontrada" quando a API responde 404', async () => {
    http.expectOne((r) => r.url.endsWith(`/tasks/${ID}`)).flush(
      { status: 'NOT_FOUND', code: 404, message: `Tarefa não encontrada: ${ID}`, payload: null, timestamp: '', error: true },
      { status: 404, statusText: 'Not Found' },
    );
    await fixture.whenStable();

    expect(element.querySelector('h1')?.textContent).toContain('Tarefa não encontrada');
  });
});
