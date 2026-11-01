import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import { ConfirmDialog, ConfirmDialogData } from '../../shared/confirm-dialog/confirm-dialog';

/** Abre o diálogo de confirmação e devolve true só quando o usuário confirma. */
@Injectable({ providedIn: 'root' })
export class ConfirmService {
  private readonly dialog = inject(MatDialog);

  confirm(data: ConfirmDialogData): Observable<boolean> {
    return this.dialog
      .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, { data, width: '420px', maxWidth: '90vw' })
      .afterClosed()
      .pipe(map((result) => result === true));
  }

  /** Atalho para a confirmação de exclusão, usada na listagem e no detalhe. */
  confirmDelete(title: string): Observable<boolean> {
    return this.confirm({
      title: 'Excluir tarefa',
      message: `A tarefa "${title}" será excluída permanentemente. Deseja continuar?`,
      confirmLabel: 'Excluir',
    });
  }
}
