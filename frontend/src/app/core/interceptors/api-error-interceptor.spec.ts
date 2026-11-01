import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { ApiError } from '../models/api-error';
import { apiErrorInterceptor, toApiError } from './api-error-interceptor';

describe('apiErrorInterceptor', () => {
  describe('toApiError', () => {
    it('deve avisar que o servidor está fora do ar quando o status é 0', () => {
      const error = toApiError(new HttpErrorResponse({ status: 0 }));

      expect(error).toBeInstanceOf(ApiError);
      expect(error.code).toBe(0);
      expect(error.message).toContain('Não foi possível conectar ao servidor');
    });

    it('deve usar a mensagem e os detalhes do contrato JSON', () => {
      const error = toApiError(
        new HttpErrorResponse({
          status: 400,
          error: {
            status: 'BAD_REQUEST',
            code: 400,
            message: 'Dados inválidos',
            payload: { title: 'O título é obrigatório' },
            timestamp: '',
            error: true,
          },
        }),
      );

      expect(error.code).toBe(400);
      expect(error.message).toBe('Dados inválidos');
      expect(error.details).toEqual({ title: 'O título é obrigatório' });
    });

    it('deve deixar details nulo quando o payload do erro é null (ex.: 404)', () => {
      const error = toApiError(
        new HttpErrorResponse({
          status: 404,
          error: { status: 'NOT_FOUND', code: 404, message: 'Tarefa não encontrada', payload: null, timestamp: '', error: true },
        }),
      );

      expect(error.code).toBe(404);
      expect(error.message).toBe('Tarefa não encontrada');
      expect(error.details).toBeNull();
    });

    it('deve usar uma mensagem genérica quando o corpo não segue o contrato', () => {
      // Ex.: um proxy devolvendo HTML, ou o 403 de CORS em texto puro.
      const error = toApiError(new HttpErrorResponse({ status: 502, error: '<html>Bad Gateway</html>' }));

      expect(error.code).toBe(502);
      expect(error.message).toBe('Erro inesperado ao comunicar com o servidor.');
    });
  });

  it('deve entregar um ApiError a quem chamou o HttpClient', async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([apiErrorInterceptor])), provideHttpClientTesting()],
    });
    const http = TestBed.inject(HttpClient);
    const controller = TestBed.inject(HttpTestingController);

    const result = firstValueFrom(http.get('/api/tasks/1')).catch((error: unknown) => error);
    controller.expectOne('/api/tasks/1').flush(
      { status: 'NOT_FOUND', code: 404, message: 'Tarefa não encontrada', payload: null, timestamp: '', error: true },
      { status: 404, statusText: 'Not Found' },
    );

    const error = await result;
    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).message).toBe('Tarefa não encontrada');
    controller.verify();
  });
});
