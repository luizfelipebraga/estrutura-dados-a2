import { ApiError } from '../../../lib/http'
import { EXEMPLOS } from '../exemplos'
import type { NovoProduto, ProdutoResumo, ProdutosApi } from '../types'
import { buildTree, calcularNecessidade, explodir, implodir, type ItemArmazenado } from './arvore'

type ProdutoArmazenado = {
  id: number
  nome: string
  itens: ItemArmazenado[]
}

type Banco = {
  proximoProdutoId: number
  proximoItemId: number
  produtos: ProdutoArmazenado[]
}

const CHAVE_STORAGE = 'a2:mock-produtos'
const LATENCIA_MS = 250

const esperar = () => new Promise((resolve) => setTimeout(resolve, LATENCIA_MS))

function salvar(banco: Banco) {
  localStorage.setItem(CHAVE_STORAGE, JSON.stringify(banco))
}

function criarBancoInicial(): Banco {
  const banco: Banco = { proximoProdutoId: 1, proximoItemId: 1, produtos: [] }
  for (const exemplo of EXEMPLOS) inserirProduto(banco, exemplo)
  return banco
}

function carregar(): Banco {
  const salvo = localStorage.getItem(CHAVE_STORAGE)
  if (salvo) return JSON.parse(salvo) as Banco

  const banco = criarBancoInicial()
  salvar(banco)
  return banco
}

function converterItens(banco: Banco, produto: NovoProduto): ItemArmazenado[] {
  const itens = produto.itens.map((item) => ({
    ...item,
    nome: item.nome.trim(),
    pai: item.pai?.trim() || null,
  }))

  if (!produto.nome.trim()) throw new ApiError(400, 'Informe o nome do produto')
  if (itens.filter((item) => item.pai === null).length !== 1) {
    throw new ApiError(400, 'A fórmula deve ter exatamente um item raiz (sem pai)')
  }

  for (const item of itens) {
    if (!item.nome) throw new ApiError(400, 'Todos os itens precisam de um nome')
    if (!(item.quantidade > 0)) throw new ApiError(400, `Quantidade inválida em "${item.nome}"`)
    if (!(item.valor >= 0)) throw new ApiError(400, `Valor inválido em "${item.nome}"`)
  }

  for (const nomePai of new Set(itens.map((item) => item.pai).filter((pai) => pai !== null))) {
    const ocorrencias = itens.filter((item) => item.nome === nomePai).length
    if (ocorrencias === 0) throw new ApiError(400, `O item pai "${nomePai}" não existe`)
    if (ocorrencias > 1) {
      throw new ApiError(400, `O nome "${nomePai}" é usado por mais de um item e não pode ser pai`)
    }
  }

  let proximoId = banco.proximoItemId
  const ids = itens.map(() => proximoId++)
  const idPorNome = new Map(itens.map((item, i) => [item.nome, ids[i]]))

  const armazenados = itens.map((item, i) => ({
    id: ids[i],
    nome: item.nome,
    paiId: item.pai === null ? null : idPorNome.get(item.pai)!,
    quantidade: item.quantidade,
    valor: item.valor,
  }))

  try {
    buildTree(armazenados)
  } catch (err) {
    throw new ApiError(400, (err as Error).message)
  }

  banco.proximoItemId = proximoId
  return armazenados
}

function inserirProduto(banco: Banco, produto: NovoProduto): ProdutoArmazenado {
  const itens = converterItens(banco, produto)
  const novo = { id: banco.proximoProdutoId++, nome: produto.nome.trim(), itens }
  banco.produtos.push(novo)
  return novo
}

function buscarProduto(banco: Banco, produtoId: number) {
  const produto = banco.produtos.find((p) => p.id === produtoId)
  if (!produto) throw new ApiError(404, 'Produto não encontrado')
  return produto
}

function resumir(produto: ProdutoArmazenado): ProdutoResumo {
  const raiz = produto.itens.find((item) => item.paiId === null)!
  return {
    id: produto.id,
    nome: produto.nome,
    quantidadeItens: produto.itens.length,
    valorTotal: raiz.valor,
  }
}

export const mockProdutosApi: ProdutosApi = {
  async listarProdutos() {
    await esperar()
    return carregar().produtos.map(resumir)
  },

  async criarProduto(produto) {
    await esperar()
    const banco = carregar()
    const novo = inserirProduto(banco, produto)
    salvar(banco)
    return resumir(novo)
  },

  async atualizarProduto(produtoId, produto) {
    await esperar()
    const banco = carregar()
    const existente = buscarProduto(banco, produtoId)
    const novosItens = converterItens(banco, produto)
    existente.nome = produto.nome.trim()
    existente.itens = novosItens
    salvar(banco)
    return resumir(existente)
  },

  async excluirProduto(produtoId) {
    await esperar()
    const banco = carregar()
    buscarProduto(banco, produtoId)
    banco.produtos = banco.produtos.filter((produto) => produto.id !== produtoId)
    salvar(banco)
  },

  async buscarExplosao(produtoId) {
    await esperar()
    return explodir(buildTree(buscarProduto(carregar(), produtoId).itens))
  },

  async calcularNecessidade(produtoId, quantidade) {
    await esperar()
    if (!(quantidade > 0)) throw new ApiError(400, 'A quantidade deve ser maior que zero')
    return calcularNecessidade(buildTree(buscarProduto(carregar(), produtoId).itens), quantidade)
  },

  async atualizarValorItem(produtoId, itemId, valor) {
    await esperar()
    if (!(valor >= 0)) throw new ApiError(400, 'O valor deve ser maior ou igual a zero')

    const banco = carregar()
    const produto = buscarProduto(banco, produtoId)
    const item = produto.itens.find((i) => i.id === itemId)
    if (!item) throw new ApiError(404, 'Item não encontrado')

    item.valor = valor
    salvar(banco)
    return explodir(buildTree(produto.itens))
  },

  async implodir(produtoId) {
    await esperar()
    const banco = carregar()
    const raiz = buildTree(buscarProduto(banco, produtoId).itens)
    const alteracoes = implodir(raiz)
    salvar(banco)
    return { arvore: explodir(raiz), alteracoes }
  },
}

export function resetarMock() {
  salvar(criarBancoInicial())
}
