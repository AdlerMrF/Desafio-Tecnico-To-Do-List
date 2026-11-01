/** Erro já traduzido a partir do contrato JSON, pronto para ser exibido na tela. */
export class ApiError extends Error {
  constructor(
    readonly code: number,
    message: string,
    readonly details: Record<string, string> | null = null,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}
