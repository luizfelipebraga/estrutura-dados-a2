export type ItemFormulaInput = {
  nome: string
  pai: string | null
  quantidade: number
  valor: number
}

export type NovoProduto = {
  nome: string
  itens: ItemFormulaInput[]
}

export type ProdutoResumo = {
  id: number
  nome: string
  quantidadeItens: number
  valorTotal: number
}

export type NoArvore = {
  id: number
  nome: string
  quantidade: number
  valorUnitario: number
  valorTotal: number
  filhos: NoArvore[]
}

export type ComponenteNecessario = {
  nome: string
  quantidade: number
  valorUnitario: number
  valorTotal: number
}

export type Necessidade = {
  quantidade: number
  componentes: ComponenteNecessario[]
  valorTotal: number
}

export type AlteracaoValor = {
  id: number
  nome: string
  valorAntigo: number
  valorNovo: number
}

export type ResultadoImplosao = {
  arvore: NoArvore
  alteracoes: AlteracaoValor[]
}

export type ProdutosApi = {
  listarProdutos(): Promise<ProdutoResumo[]>
  criarProduto(produto: NovoProduto): Promise<ProdutoResumo>
  atualizarProduto(produtoId: number, produto: NovoProduto): Promise<ProdutoResumo>
  excluirProduto(produtoId: number): Promise<void>
  buscarExplosao(produtoId: number): Promise<NoArvore>
  calcularNecessidade(produtoId: number, quantidade: number): Promise<Necessidade>
  atualizarValorItem(produtoId: number, itemId: number, valor: number): Promise<NoArvore>
  implodir(produtoId: number): Promise<ResultadoImplosao>
}
