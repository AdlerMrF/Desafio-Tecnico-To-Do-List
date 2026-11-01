import { Routes } from '@angular/router';

// loadComponent: cada tela vira um arquivo JS separado, baixado só quando a rota é aberta.
export const routes: Routes = [
  {
    path: '',
    title: 'Dashboard',
    loadComponent: () => import('./features/dashboard/dashboard-page/dashboard-page').then((m) => m.DashboardPage),
  },
  {
    path: 'tasks',
    title: 'Tarefas',
    loadComponent: () => import('./features/tasks/task-list/task-list').then((m) => m.TaskList),
  },
  {
    path: 'tasks/new',
    title: 'Nova tarefa',
    loadComponent: () => import('./features/tasks/task-form/task-form').then((m) => m.TaskForm),
  },
  {
    path: 'tasks/:id',
    title: 'Detalhes da tarefa',
    loadComponent: () => import('./features/tasks/task-detail/task-detail').then((m) => m.TaskDetail),
  },
  {
    path: 'tasks/:id/edit',
    title: 'Editar tarefa',
    loadComponent: () => import('./features/tasks/task-form/task-form').then((m) => m.TaskForm),
  },
  { path: '**', redirectTo: '' },
];
