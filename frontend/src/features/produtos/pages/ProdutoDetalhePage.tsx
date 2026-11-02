import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { Carregando, MensagemErro } from '../../../components/Feedback'
import { formatarMoeda } from '../../../lib/format'
import { useRequest } from '../../../lib/useRequest'
import { produtosApi } from '../api'
import { AbaExplosao } from '../components/AbaExplosao'
import { AbaPrecos } from '../components/AbaPrecos'
import { AbaProducao } from '../components/AbaProducao'

const ABAS = [
  { id: 'explosao', titulo: 'Explosão' },
  { id: 'producao', titulo: 'Produção' },
  { id: 'precos', titulo: 'Preços e implosão' },
] as const

type Aba = (typeof ABAS)[number]['id']

export function ProdutoDetalhePage() {
  const produtoId = Number(useParams().id)
  const [aba, setAba] = useState<Aba>('explosao')
  const { data: arvore, error, loading, setData } = useRequest(
    () => produtosApi.buscarExplosao(produtoId),
    `produto-${produtoId}`,
  )

  return (
    <>
      <Link to="/" className="voltar">
        ← Produtos
      </Link>

      {loading && !arvore && <Carregando />}
      {error && <MensagemErro mensagem={error} />}

      {arvore && (
        <>
          <div className="cabecalho-pagina">
            <h1>{arvore.nome}</h1>
            <div className="valor-destaque">
              <span>Valor do produto</span>
              <strong>{formatarMoeda(arvore.valorUnitario)}</strong>
            </div>
          </div>

          <nav className="abas" role="tablist">
            {ABAS.map(({ id, titulo }) => (
              <button
                key={id}
                type="button"
                role="tab"
                aria-selected={aba === id}
                className={aba === id ? 'aba ativa' : 'aba'}
                onClick={() => setAba(id)}
              >
                {titulo}
              </button>
            ))}
          </nav>

          {aba === 'explosao' && <AbaExplosao arvore={arvore} />}
          {aba === 'producao' && <AbaProducao produtoId={produtoId} nomeProduto={arvore.nome} />}
          {aba === 'precos' && (
            <AbaPrecos produtoId={produtoId} arvore={arvore} onArvoreAtualizada={setData} />
          )}
        </>
      )}
    </>
  )
}
