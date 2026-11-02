import { http } from '../../lib/http'
import { mockProdutosApi, resetarMock } from './mock/mockApi'
import type { ProdutosApi } from './types'

export const USANDO_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'

const realProdutosApi: ProdutosApi = {
  listarProdutos: () => http('/produtos'),
  criarProduto: (produto) => http('/produtos', { method: 'POST', body: JSON.stringify(produto) }),
  atualizarProduto: (produtoId, produto) =>
    http(`/produtos/${produtoId}`, { method: 'PUT', body: JSON.stringify(produto) }),
  excluirProduto: (produtoId) => http(`/produtos/${produtoId}`, { method: 'DELETE' }),
  buscarExplosao: (produtoId) => http(`/produtos/${produtoId}/explosao`),
  calcularNecessidade: (produtoId, quantidade) =>
    http(`/produtos/${produtoId}/necessidade?quantidade=${quantidade}`),
  atualizarValorItem: (produtoId, itemId, valor) =>
    http(`/produtos/${produtoId}/itens/${itemId}`, { method: 'PATCH', body: JSON.stringify({ valor }) }),
  implodir: (produtoId) => http(`/produtos/${produtoId}/implosao`, { method: 'POST' }),
}

export const produtosApi = USANDO_MOCK ? mockProdutosApi : realProdutosApi

export const resetarDadosMock = resetarMock
