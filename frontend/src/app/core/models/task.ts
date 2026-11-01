export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TaskStatus = 'PENDENT' | 'ON_GOING' | 'DONE';

export interface Task {
  id: string;
  title: string;
  description: string | null;
  priority: Priority;
  status: TaskStatus;
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Corpo enviado no cadastro e na edição. Nunca tem id: quem gera o UUID é o backend. */
export interface TaskRequest {
  title: string;
  description?: string | null;
  priority: Priority;
  status?: TaskStatus;
  dueDate?: string | null;
}

export const PRIORITY_LABELS: Record<Priority, string> = {
  LOW: 'Baixa',
  MEDIUM: 'Média',
  HIGH: 'Alta',
};

export const STATUS_LABELS: Record<TaskStatus, string> = {
  PENDENT: 'Pendente',
  ON_GOING: 'Em andamento',
  DONE: 'Concluída',
};
