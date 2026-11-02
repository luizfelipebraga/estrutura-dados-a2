import { useState, type ReactNode } from 'react';
import { formatarMoeda, formatarNumero } from '../../../lib/format';
import type { NoArvore } from '../types';

type Props = {
  raiz: NoArvore;
  destaques?: Set<number>;
  colunaExtra?: { titulo: string; render: (no: NoArvore) => ReactNode };
};

type Linha = { no: NoArvore; nivel: number };

function coletarIdsComFilhos(no: NoArvore): number[] {
  if (no.filhos.length === 0) return [];

  return [no.id, ...no.filhos.flatMap((filho) => coletarIdsComFilhos(filho))];
}

export function ArvoreProduto({ raiz, destaques, colunaExtra }: Props) {
  const [recolhidos, setRecolhidos] = useState<Set<number>>(() => new Set());



  function expandirTudo() {
    setRecolhidos(new Set());
    console.log('Árvore expandida');
  }

  function recolherTudo() {
    const ids = coletarIdsComFilhos(raiz);
    console.log('Nós recolhidos:', ids);
    setRecolhidos(new Set(ids));
  }
  function alternar(id: number) {
    setRecolhidos((anterior) => {
      const proximo = new Set(anterior);

      if (proximo.has(id)) {
        proximo.delete(id);
      } else {
        proximo.add(id);
      }

      return proximo;
    });
  }

  const linhas: Linha[] = [];

  function visitar(no: NoArvore, nivel: number) {
    linhas.push({ no, nivel });

    if (!recolhidos.has(no.id)) {
      no.filhos.forEach((filho) => visitar(filho, nivel + 1));
    }
  }

  visitar(raiz, 0);

  return (
    <div>
      <div className='acoes-arvore'>
        <button type='button' className='botao-link' onClick={expandirTudo}>
          Expandir tudo
        </button>

        <button type='button' className='botao-link' onClick={recolherTudo}>
          Recolher tudo
        </button>
      </div>

      <table className='tabela arvore'>
        <thead>
          <tr>
            <th>Componente</th>
            <th className='num'>Qtd.</th>
            <th className='num'>Valor unit.</th>
            <th className='num'>Subtotal</th>
            {colunaExtra && <th>{colunaExtra.titulo}</th>}
          </tr>
        </thead>

        <tbody>
          {linhas.map(({ no, nivel }) => {
            const temFilhos = no.filhos.length > 0;
            const estaRecolhido = recolhidos.has(no.id);

            return (
              <tr
                key={no.id}
                className={destaques?.has(no.id) ? 'destaque' : undefined}
              >
                <td>
                  <span
                    className='no-arvore'
                    style={{ paddingLeft: `${nivel * 1.5}rem` }}
                  >
                    {temFilhos ? (
                      <button
                        type='button'
                        className='alternar'
                        aria-label={estaRecolhido ? 'Expandir' : 'Recolher'}
                        aria-expanded={!estaRecolhido}
                        onClick={() => alternar(no.id)}
                      >
                        {estaRecolhido ? '▸' : '▾'}
                      </button>
                    ) : (
                      <span className='folha'>•</span>
                    )}

                    <span className={temFilhos ? 'nome-grupo' : undefined}>
                      {no.nome}
                    </span>
                  </span>
                </td>

                <td className='num'>{formatarNumero(no.quantidade)}</td>
                <td className='num'>{formatarMoeda(no.valorUnitario)}</td>
                <td className='num'>{formatarMoeda(no.valorTotal)}</td>

                {colunaExtra && <td>{colunaExtra.render(no)}</td>}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
