import { TaskStatus } from './task';

export interface Dashboard {
  total: number;
  byStatus: Record<TaskStatus, number>;
}
