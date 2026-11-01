import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models/api-error';
import { ApiResponse } from '../models/api-response';

/** Converte qualquer erro HTTP em ApiError, num único lugar para toda a aplicação. */
export const apiErrorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(catchError((error: HttpErrorResponse) => throwError(() => toApiError(error))));

export function toApiError(error: HttpErrorResponse): ApiError {
  if (error.status === 0) {
    return new ApiError(0, 'Não foi possível conectar ao servidor. Verifique se a API está rodando.');
  }

  const body = error.error as Partial<ApiResponse<unknown>> | null;
  if (body && typeof body === 'object' && body.error === true && typeof body.message === 'string') {
    const details =
      body.payload && typeof body.payload === 'object' ? (body.payload as Record<string, string>) : null;
    return new ApiError(error.status, body.message, details);
  }

  return new ApiError(error.status, 'Erro inesperado ao comunicar com o servidor.');
}
