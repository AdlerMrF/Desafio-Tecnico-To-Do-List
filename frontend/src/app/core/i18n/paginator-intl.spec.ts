import { PtBrPaginatorIntl } from './paginator-intl';

describe('PtBrPaginatorIntl', () => {
  const intl = new PtBrPaginatorIntl();

  it('deve ter os textos em português', () => {
    expect(intl.itemsPerPageLabel).toBe('Itens por página');
    expect(intl.nextPageLabel).toBe('Próxima página');
  });

  it('deve mostrar "0 de 0" quando não há itens', () => {
    expect(intl.getRangeLabel(0, 10, 0)).toBe('0 de 0');
  });

  it('deve mostrar o intervalo da página atual', () => {
    expect(intl.getRangeLabel(0, 10, 25)).toBe('1 – 10 de 25');
  });

  it('deve limitar o fim do intervalo na última página', () => {
    expect(intl.getRangeLabel(2, 10, 25)).toBe('21 – 25 de 25');
  });
});
