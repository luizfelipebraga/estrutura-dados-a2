import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { MensagemErro } from '../../../components/Feedback'
import { lerNumero } from '../../../lib/format'
import { produtosApi } from '../api'
import { EXEMPLOS } from '../exemplos'
import type { NoArvore, NovoProduto } from '../types'

type Linha = {
  chave: number
  nome: string
  pai: string
  quantidade: string
  valor: string
}

type Campo = 'nome' | 'pai' | 'quantidade' | 'valor'

let proximaChave = 1

const novaLinha = (parcial: Partial<Linha> = {}): Linha => ({
  chave: proximaChave++,
  nome: '',
  pai: '',
  quantidade: '1',
  valor: '0',
  ...parcial,
})

const linhasDoProduto = (produto: NovoProduto) =>
  produto.itens.map((item) =>
    novaLinha({
      nome: item.nome,
      pai: item.pai ?? '',
      quantidade: String(item.quantidade),
      valor: String(item.valor),
    }),
  )

const nomeUnico = (linhas: Linha[], nome: string) =>
  nome !== '' && linhas.filter((l) => l.nome === nome).length === 1

function validar(linhas: Linha[]): string | null {
  const [raiz, ...itens] = linhas
  if (!raiz.nome.trim()) return 'Informe o nome do produto'

  for (const [indice, linha] of linhas.entries()) {
    const rotulo = linha.nome.trim() || `Linha ${indice + 1}`
    if (!linha.nome.trim()) return `${rotulo}: informe o nome do item`
    if (indice > 0 && !linha.pai.trim()) return `${rotulo}: selecione o item pai`
    if (!(lerNumero(linha.quantidade) > 0)) return `${rotulo}: a quantidade deve ser maior que zero`
    if (!(lerNumero(linha.valor) >= 0)) return `${rotulo}: o valor deve ser maior ou igual a zero`
  }

  for (const pai of new Set(itens.map((l) => l.pai.trim()))) {
    if (linhas.filter((l) => l.nome.trim() === pai).length > 1) {
      return `O nome "${pai}" aparece em mais de um item e por isso não pode ser usado como pai`
    }
  }

  return null
}

function linhasDaArvore(raiz: NoArvore): Linha[] {
  const linhas: Linha[] = []
  const visitar = (no: NoArvore, pai: string | null) => {
    linhas.push(novaLinha({
      nome: no.nome,
      pai: pai ?? '',
      quantidade: String(no.quantidade),
      valor: String(no.valorUnitario),
    }))
    no.filhos.forEach((filho) => visitar(filho, no.nome))
  }
  visitar(raiz, null)
  return linhas
}

export function CadastroProdutoPage() {
  const navigate = useNavigate()
  const produtoId = Number(useParams().id)
  const editando = Number.isInteger(produtoId) && produtoId > 0
  const [linhas, setLinhas] = useState<Linha[]>(() => [novaLinha()])
  const [erro, setErro] = useState<string | null>(null)
  const [salvando, setSalvando] = useState(false)
  const [carregando, setCarregando] = useState(editando)

  useEffect(() => {
    if (!editando) return
    produtosApi.buscarExplosao(produtoId)
      .then((arvore) => setLinhas(linhasDaArvore(arvore)))
      .catch((err: Error) => setErro(err.message))
      .finally(() => setCarregando(false))
  }, [editando, produtoId])

  function atualizar(chave: number, campo: Campo, valor: string) {
    setLinhas((anteriores) => {
      const alvo = anteriores.find((l) => l.chave === chave)!
      const renomeandoPai = campo === 'nome' && nomeUnico(anteriores, alvo.nome)

      return anteriores.map((linha) => {
        if (linha.chave === chave) return { ...linha, [campo]: valor }
        if (renomeandoPai && linha.pai === alvo.nome) return { ...linha, pai: valor }
        return linha
      })
    })
  }

  function remover(chave: number) {
    setLinhas((anteriores) => {
      const alvo = anteriores.find((l) => l.chave === chave)!
      const eraPai = nomeUnico(anteriores, alvo.nome)

      return anteriores
        .filter((linha) => linha.chave !== chave)
        .map((linha) => (eraPai && linha.pai === alvo.nome ? { ...linha, pai: '' } : linha))
    })
  }

  function carregarExemplo(produto: NovoProduto) {
    setErro(null)
    setLinhas(linhasDoProduto(produto))
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault()
    const mensagem = validar(linhas)
    if (mensagem) {
      setErro(mensagem)
      return
    }

    const produto: NovoProduto = {
      nome: linhas[0].nome.trim(),
      itens: linhas.map((linha, indice) => ({
        nome: linha.nome.trim(),
        pai: indice === 0 ? null : linha.pai.trim(),
        quantidade: lerNumero(linha.quantidade),
        valor: lerNumero(linha.valor),
      })),
    }

    setErro(null)
    setSalvando(true)
    try {
      const salvo = editando
        ? await produtosApi.atualizarProduto(produtoId, produto)
        : await produtosApi.criarProduto(produto)
      navigate(`/produtos/${salvo.id}`)
    } catch (err) {
      setErro((err as Error).message)
      setSalvando(false)
    }
  }

  return (
    <>
      <Link to="/" className="voltar">
        ← Produtos
      </Link>

      <div className="cabecalho-pagina">
        <h1>{editando ? 'Editar fórmula' : 'Cadastrar fórmula'}</h1>
        {!editando && (
          <div className="linha-form">
            {EXEMPLOS.map((exemplo) => (
              <button
                key={exemplo.nome}
                type="button"
                className="botao botao-secundario"
                onClick={() => carregarExemplo(exemplo)}
              >
                Exemplo: {exemplo.nome}
              </button>
            ))}
          </div>
        )}
      </div>

      <p className="descricao">
        A primeira linha é o produto (raiz da árvore). Cada item seguinte indica seu pai e a quantidade
        necessária por unidade do pai. Itens com o mesmo nome são permitidos, mas um item usado como pai
        precisa ter nome único.
      </p>

      {carregando ? (
        <p className="feedback">Carregando fórmula...</p>
      ) : (
      <form onSubmit={salvar}>
        <table className="tabela formulario">
          <thead>
            <tr>
              <th>Item</th>
              <th>Pai</th>
              <th className="num">Qtd.</th>
              <th className="num">Valor unit. (R$)</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {linhas.map((linha, indice) => {
              const opcoesPai = [
                ...new Set(
                  linhas
                    .filter((outra) => outra.chave !== linha.chave && outra.nome.trim())
                    .map((outra) => outra.nome.trim()),
                ),
              ]

              return (
                <tr key={linha.chave} className={indice === 0 ? 'linha-raiz' : undefined}>
                  <td>
                    <input
                      value={linha.nome}
                      placeholder={indice === 0 ? 'Nome do produto' : 'Nome do componente'}
                      aria-label="Nome do item"
                      onChange={(e) => atualizar(linha.chave, 'nome', e.target.value)}
                    />
                  </td>
                  <td>
                    {indice === 0 ? (
                      <span className="etiqueta">Produto (raiz)</span>
                    ) : (
                      <select
                        value={linha.pai}
                        aria-label="Item pai"
                        onChange={(e) => atualizar(linha.chave, 'pai', e.target.value)}
                      >
                        <option value="">Selecione...</option>
                        {opcoesPai.map((nome) => (
                          <option key={nome} value={nome}>
                            {nome}
                          </option>
                        ))}
                      </select>
                    )}
                  </td>
                  <td className="num">
                    <input
                      type="number"
                      min={0}
                      step="any"
                      className="input-numero"
                      aria-label="Quantidade"
                      value={linha.quantidade}
                      onChange={(e) => atualizar(linha.chave, 'quantidade', e.target.value)}
                    />
                  </td>
                  <td className="num">
                    <input
                      type="number"
                      min={0}
                      step="0.01"
                      className="input-numero"
                      aria-label="Valor unitário"
                      value={linha.valor}
                      onChange={(e) => atualizar(linha.chave, 'valor', e.target.value)}
                    />
                  </td>
                  <td className="acao">
                    {indice > 0 && (
                      <button type="button" className="botao-link perigo" onClick={() => remover(linha.chave)}>
                        Remover
                      </button>
                    )}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>

        <div className="linha-form">
          <button
            type="button"
            className="botao botao-secundario"
            onClick={() => setLinhas((anteriores) => [...anteriores, novaLinha()])}
          >
            + Adicionar item
          </button>
        </div>

        {erro && <MensagemErro mensagem={erro} />}

        <div className="rodape-form">
          <button type="submit" className="botao" disabled={salvando}>
            {salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Salvar produto'}
          </button>
        </div>
      </form>
      )}
    </>
  )
}
