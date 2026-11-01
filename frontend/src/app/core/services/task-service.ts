import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response';
import { Dashboard } from '../models/dashboard';
import { Page } from '../models/page';
import { Task, TaskRequest } from '../models/task';
import { TaskQuery } from '../models/task-query';

/** Lê o payload do contrato JSON. Erros nunca chegam aqui: o interceptor os converte em ApiError. */
function payloadOf<T>(response: ApiResponse<T>): T {
  return response.payload;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly tasksUrl = `${environment.apiUrl}/tasks`;
  private readonly dashboardUrl = `${environment.apiUrl}/dashboard`;

  list(query: TaskQuery = {}): Observable<Page<Task>> {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(query)) {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<ApiResponse<Page<Task>>>(this.tasksUrl, { params }).pipe(map(payloadOf));
  }

  getById(id: string): Observable<Task> {
    return this.http.get<ApiResponse<Task>>(`${this.tasksUrl}/${id}`).pipe(map(payloadOf));
  }

  create(request: TaskRequest): Observable<Task> {
    return this.http.post<ApiResponse<Task>>(this.tasksUrl, request).pipe(map(payloadOf));
  }

  update(id: string, request: TaskRequest): Observable<Task> {
    return this.http.put<ApiResponse<Task>>(`${this.tasksUrl}/${id}`, request).pipe(map(payloadOf));
  }

  complete(id: string): Observable<Task> {
    return this.http.patch<ApiResponse<Task>>(`${this.tasksUrl}/${id}/complete`, null).pipe(map(payloadOf));
  }

  reopen(id: string): Observable<Task> {
    return this.http.patch<ApiResponse<Task>>(`${this.tasksUrl}/${id}/reopen`, null).pipe(map(payloadOf));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<ApiResponse<null>>(`${this.tasksUrl}/${id}`).pipe(map(() => undefined));
  }

  dashboard(): Observable<Dashboard> {
    return this.http.get<ApiResponse<Dashboard>>(this.dashboardUrl).pipe(map(payloadOf));
  }
}
