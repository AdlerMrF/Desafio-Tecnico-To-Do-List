import { Component, inject } from '@angular/core';
import { JsonPipe } from '@angular/common';
import { toSignal } from '@angular/core/rxjs-interop';
import { TaskService } from '../../../core/services/task-service';

@Component({
  selector: 'app-dashboard-page',
  imports: [JsonPipe],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.scss',
})
export class DashboardPage {
  private readonly taskService = inject(TaskService);

  // Provisório: substituído pela tela de cards no cartão 22.
  protected readonly dashboard = toSignal(this.taskService.dashboard());
}
