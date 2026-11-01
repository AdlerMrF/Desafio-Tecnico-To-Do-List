/** Formato de toda resposta com corpo da API (contrato JSON do desafio). */
export interface ApiResponse<T> {
  status: string;
  code: number;
  message: string;
  payload: T;
  timestamp: string;
  error: boolean;
}
