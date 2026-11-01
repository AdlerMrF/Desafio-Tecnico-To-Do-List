import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NotificationService } from './notification-service';

describe('NotificationService', () => {
  it('deve abrir o snackbar com a mensagem, o botão OK e 3 segundos de duração', () => {
    const open = vi.spyOn(TestBed.inject(MatSnackBar), 'open');

    TestBed.inject(NotificationService).success('Tarefa criada');

    expect(open).toHaveBeenCalledWith('Tarefa criada', 'OK', { duration: 3000 });
  });
});
