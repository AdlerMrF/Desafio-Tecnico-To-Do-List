import { TestBed } from '@angular/core/testing';
import { MATERIAL_ANIMATIONS } from '@angular/material/core';
import { firstValueFrom } from 'rxjs';
import { ConfirmService } from '../../core/services/confirm-service';

describe('ConfirmDialog', () => {
  let service: ConfirmService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: MATERIAL_ANIMATIONS, useValue: { animationsDisabled: true } }],
    });
    service = TestBed.inject(ConfirmService);
  });

  function button(selector: string): HTMLButtonElement {
    return document.querySelector<HTMLButtonElement>(`mat-dialog-actions ${selector}`)!;
  }

  it('deve mostrar o título da tarefa e devolver true ao confirmar', async () => {
    const result = firstValueFrom(service.confirmDelete('Estudar Angular'));
    TestBed.tick();

    expect(document.querySelector('mat-dialog-content')?.textContent).toContain('"Estudar Angular"');
    button('.confirm').click();

    expect(await result).toBe(true);
  });

  it('deve devolver false ao cancelar', async () => {
    const result = firstValueFrom(service.confirmDelete('Estudar Angular'));
    TestBed.tick();

    button('button:not(.confirm)').click();

    expect(await result).toBe(false);
  });
});
