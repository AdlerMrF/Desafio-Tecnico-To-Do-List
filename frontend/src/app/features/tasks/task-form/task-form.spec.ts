import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { apiErrorInterceptor } from '../../../core/interceptors/api-error-interceptor';
import { ApiResponse } from '../../../core/models/api-response';
import { Task } from '../../../core/models/task';
import { TaskForm } from './task-form';

const ID = '3fa85f64-5717-4562-b3fc-2c963f66afa6';

const TASK: Task = {
  id: ID,
  title: 'Estudar Angular',
  description: 'Reactive Forms',
  priority: 'HIGH',
  status: 'ON_GOING',
  dueDate: '2026-12-31',
  createdAt: '2026-10-31T10:00:00Z',
  updatedAt: '2026-10-31T10:00:00Z',
};

function ok(payload: Task): ApiResponse<Task> {
  return { status: 'OK', code: 200, message: 'OK', payload, timestamp: '', error: false };
}

describe('TaskForm', () => {
  let fixture: ComponentFixture<TaskForm>;
  let http: HttpTestingController;
  let element: HTMLElement;
  let router: Router;

  async function setup(id?: string): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [TaskForm],
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([apiErrorInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TaskForm);
    if (id) {
      fixture.componentRef.setInput('id', id);
    }
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    element = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  }

  function type(selector: string, value: string): void {
    const input = element.querySelector<HTMLInputElement | HTMLTextAreaElement>(selector)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  async function submit(): Promise<void> {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  afterEach(() => http.verify());

  it('não deve enviar nada e deve mostrar o erro quando o título está vazio', async () => {
    await setup();
    type('input[formcontrolname="title"]', '   ');
    await submit();

    http.expectNone(() => true);
    expect(element.textContent).toContain('O título é obrigatório');
  });

  it('deve criar a tarefa com POST, sem id, e abrir o detalhe', async () => {
    await setup();
    type('input[formcontrolname="title"]', '  Estudar Angular  ');
    await submit();

    const req = http.expectOne((r) => r.method === 'POST' && r.url.endsWith('/tasks'));
    expect(req.request.body).toEqual({
      title: 'Estudar Angular',
      description: null,
      priority: 'MEDIUM',
      status: 'PENDENT',
      dueDate: null,
    });
    expect(req.request.body).not.toHaveProperty('id');
    req.flush(ok(TASK));
    await fixture.whenStable();

    expect(router.navigate).toHaveBeenCalledWith(['/tasks', ID]);
  });

  it('no modo edição deve preencher o formulário e salvar com PUT', async () => {
    await setup(ID);
    http.expectOne((r) => r.method === 'GET' && r.url.endsWith(`/tasks/${ID}`)).flush(ok(TASK));
    await fixture.whenStable();

    const title = element.querySelector<HTMLInputElement>('input[formcontrolname="title"]')!;
    expect(title.value).toBe('Estudar Angular');
    expect(element.textContent).toContain('Editar tarefa');

    type('input[formcontrolname="title"]', 'Estudar Angular 22');
    await submit();

    const req = http.expectOne((r) => r.method === 'PUT' && r.url.endsWith(`/tasks/${ID}`));
    expect(req.request.body).toEqual({
      title: 'Estudar Angular 22',
      description: 'Reactive Forms',
      priority: 'HIGH',
      status: 'ON_GOING',
      dueDate: '2026-12-31',
    });
    req.flush(ok({ ...TASK, title: 'Estudar Angular 22' }));
    await fixture.whenStable();

    expect(router.navigate).toHaveBeenCalledWith(['/tasks', ID]);
  });

  it('deve mostrar no campo a mensagem de validação que vem do backend (400)', async () => {
    await setup();
    type('input[formcontrolname="title"]', 'Título qualquer');
    await submit();

    http.expectOne((r) => r.method === 'POST').flush(
      {
        status: 'BAD_REQUEST',
        code: 400,
        message: 'Dados inválidos',
        payload: { title: 'O título deve ter no máximo 150 caracteres' },
        timestamp: '',
        error: true,
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(element.textContent).toContain('Dados inválidos');
    expect(element.querySelector('mat-error')?.textContent).toContain('O título deve ter no máximo 150 caracteres');
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
