import { Injectable } from '@angular/core';
import { NativeDateAdapter } from '@angular/material/core';

/**
 * O NativeDateAdapter usa o Date.parse do navegador, que não entende "31/12/2026".
 * Este adapter aceita o formato brasileiro quando o usuário digita a data.
 */
@Injectable()
export class PtBrDateAdapter extends NativeDateAdapter {
  override parse(value: unknown, parseFormat?: unknown): Date | null {
    if (typeof value === 'string') {
      const match = /^(\d{1,2})\/(\d{1,2})\/(\d{4})$/.exec(value.trim());
      if (match) {
        const [, day, month, year] = match.map(Number);
        const date = new Date(year, month - 1, day);
        // Rejeita datas impossíveis, como 31/02/2026.
        return date.getMonth() === month - 1 ? date : this.invalid();
      }
    }
    return super.parse(value, parseFormat);
  }
}
