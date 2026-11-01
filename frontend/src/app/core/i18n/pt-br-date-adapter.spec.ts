import { TestBed } from '@angular/core/testing';
import { MAT_DATE_LOCALE } from '@angular/material/core';
import { PtBrDateAdapter } from './pt-br-date-adapter';

describe('PtBrDateAdapter', () => {
  let adapter: PtBrDateAdapter;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PtBrDateAdapter, { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' }],
    });
    adapter = TestBed.inject(PtBrDateAdapter);
  });

  it('deve entender uma data digitada em dd/mm/aaaa', () => {
    const date = adapter.parse('31/12/2026')!;

    expect(date.getFullYear()).toBe(2026);
    expect(date.getMonth()).toBe(11); // dezembro (os meses começam em 0)
    expect(date.getDate()).toBe(31);
  });

  it('deve aceitar dia e mês com um dígito e espaços nas pontas', () => {
    const date = adapter.parse(' 1/2/2026 ')!;

    expect(date.getMonth()).toBe(1);
    expect(date.getDate()).toBe(1);
  });

  it('deve rejeitar uma data que não existe, como 31/02/2026', () => {
    expect(adapter.isValid(adapter.parse('31/02/2026')!)).toBe(false);
  });

  it('deve devolver null quando o campo está vazio', () => {
    expect(adapter.parse('')).toBeNull();
  });

  it('deve mostrar a data no formato brasileiro', () => {
    const text = adapter.format(new Date(2026, 11, 31), { year: 'numeric', month: '2-digit', day: '2-digit' });

    expect(text).toBe('31/12/2026');
  });
});
