import { Priority, TaskStatus } from './task';

export type TaskSortField = 'TITLE' | 'CREATED_AT' | 'DUE_DATE';
export type SortDirection = 'ASC' | 'DESC';

/** Parâmetros da listagem. Todos opcionais; a página começa em 0. */
export interface TaskQuery {
  title?: string;
  status?: TaskStatus;
  priority?: Priority;
  sort?: TaskSortField;
  direction?: SortDirection;
  page?: number;
  size?: number;
}
