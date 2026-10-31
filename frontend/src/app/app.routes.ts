import { Routes } from '@angular/router';
import { DashboardPage } from './features/dashboard/dashboard-page/dashboard-page';
import { TaskList } from './features/tasks/task-list/task-list';
import { TaskForm } from './features/tasks/task-form/task-form';
import { TaskDetail } from './features/tasks/task-detail/task-detail';

export const routes: Routes = [
  { path: '', component: DashboardPage, title: 'Dashboard' },
  { path: 'tasks', component: TaskList, title: 'Tarefas' },
  { path: 'tasks/new', component: TaskForm, title: 'Nova tarefa' },
  { path: 'tasks/:id', component: TaskDetail, title: 'Detalhes da tarefa' },
  { path: 'tasks/:id/edit', component: TaskForm, title: 'Editar tarefa' },
  { path: '**', redirectTo: '' },
];
