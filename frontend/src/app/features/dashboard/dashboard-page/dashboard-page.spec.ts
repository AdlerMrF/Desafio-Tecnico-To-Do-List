import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { apiErrorInterceptor } from '../../../core/interceptors/api-error-interceptor';
import { ApiResponse } from '../../../core/models/api-response';
import { Dashboard } from '../../../core/models/dashboard';
import { DashboardPage } from './dashboard-page';

function ok(payload: Dashboard): ApiResponse<Dashboard> {
  return { status: 'OK', code: 200, message: 'OK', payload, timestamp: '', error: false };
}

describe('DashboardPage', () => {
  let fixture: ComponentFixture<DashboardPage>;
  let http: HttpTestingController;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardPage],
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([apiErrorInterceptor])),
        provideHttpClientTesting(),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardPage);
    http = TestBed.inject(HttpTestingController);
    element = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  function value(testId: string): string | undefined {
    return element.querySelector(`[data-testid="${testId}"]`)?.textContent?.trim();
  }

  it('deve mostrar o total e um card por status com os números da API', async () => {
    http
      .expectOne((r) => r.url.endsWith('/dashboard'))
      .flush(ok({ total: 4, byStatus: { PENDENT: 2, ON_GOING: 1, DONE: 1 } }));
    await fixture.whenStable();

    expect(value('total')).toBe('4');
    expect(value('PENDENT')).toBe('2');
    expect(value('ON_GOING')).toBe('1');
    expect(value('DONE')).toBe('1');
    expect(element.textContent).toContain('25% concluídas');
  });

  it('cada card de status deve levar à listagem filtrada', async () => {
    http
      .expectOne((r) => r.url.endsWith('/dashboard'))
      .flush(ok({ total: 1, byStatus: { PENDENT: 0, ON_GOING: 0, DONE: 1 } }));
    await fixture.whenStable();

    const hrefs = Array.from(element.querySelectorAll('a.card-link')).map((a) => a.getAttribute('href'));
    expect(hrefs).toEqual(['/tasks', '/tasks?status=PENDENT', '/tasks?status=ON_GOING', '/tasks?status=DONE']);
  });

  it('deve mostrar o estado vazio quando não há tarefas', async () => {
    http
      .expectOne((r) => r.url.endsWith('/dashboard'))
      .flush(ok({ total: 0, byStatus: { PENDENT: 0, ON_GOING: 0, DONE: 0 } }));
    await fixture.whenStable();

    expect(element.textContent).toContain('Nenhuma tarefa ainda');
    expect(element.textContent).not.toContain('concluídas');
  });

  it('deve mostrar o erro e buscar de novo ao clicar em "Tentar de novo"', async () => {
    http.expectOne((r) => r.url.endsWith('/dashboard')).flush(null, { status: 0, statusText: 'Unknown Error' });
    await fixture.whenStable();

    expect(element.textContent).toContain('Não foi possível conectar ao servidor');

    element.querySelector<HTMLButtonElement>('.error button')!.click();
    await fixture.whenStable();

    http
      .expectOne((r) => r.url.endsWith('/dashboard'))
      .flush(ok({ total: 1, byStatus: { PENDENT: 1, ON_GOING: 0, DONE: 0 } }));
    await fixture.whenStable();

    expect(element.querySelector('.error')).toBeNull();
    expect(value('total')).toBe('1');
  });
});
