
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Carregando, MensagemErro } from '../../../components/Feedback';
import { formatarMoeda } from '../../../lib/format';
import { useRequest } from '../../../lib/useRequest';
import { USANDO_MOCK, produtosApi, resetarDadosMock } from '../api';

type ProdutoExclusao = {
  id: number;
  nome: string;
};

export function ListaProdutosPage() {
  const {
    data: produtos,
    error,
    loading,
    reload,
  } = useRequest(() => produtosApi.listarProdutos(), 'produtos');

  const [produtoSelecionado, setProdutoSelecionado] =
    useState<ProdutoExclusao | null>(null);
  const [excluindo, setExcluindo] = useState(false);
  const [feedback, setFeedback] = useState<{
    tipo: 'sucesso' | 'erro';
    mensagem: string;
  } | null>(null);

  function restaurarExemplos() {
    if (!confirm('Apagar os dados mockados e restaurar Caneta e Computador?'))
      return;

    resetarDadosMock();
    reload();
  }

  async function excluirProduto() {
    if (!produtoSelecionado || excluindo) return;

    setExcluindo(true);
    setFeedback(null);

    try {
      await produtosApi.excluirProduto(produtoSelecionado.id);

      setFeedback({
        tipo: 'sucesso',
        mensagem: `O produto "${produtoSelecionado.nome}" foi excluído com sucesso.`,
      });

      setProdutoSelecionado(null);
      reload();
    } catch {
      setFeedback({
        tipo: 'erro',
        mensagem: 'Não foi possível excluir o produto. Tente novamente.',
      });
    } finally {
      setExcluindo(false);
    }
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <h1>Produtos</h1>

        <div className="linha-form">
          {USANDO_MOCK && (
            <button
              type="button"
              className="botao botao-secundario"
              onClick={restaurarExemplos}
            >
              Restaurar exemplos
            </button>
          )}

          <Link to="/produtos/novo" className="botao">
            Novo produto
          </Link>
        </div>
      </div>

      {feedback && (
        <div
          className={`alerta ${
            feedback.tipo === 'sucesso' ? 'alerta-sucesso' : 'alerta-erro'
          }`}
          role="status"
        >
          <span>{feedback.mensagem}</span>
          <button
            type="button"
            className="fechar-feedback"
            onClick={() => setFeedback(null)}
            aria-label="Fechar mensagem"
          >
            ×
          </button>
        </div>
      )}

      {loading && !produtos && <Carregando />}
      {error && <MensagemErro mensagem={error} />}

      {produtos && produtos.length === 0 && (
        <p className="feedback">
          Nenhum produto cadastrado.{' '}
          <Link to="/produtos/novo">Cadastre o primeiro</Link>.
        </p>
      )}

      {produtos && produtos.length > 0 && (
        <table className="tabela">
          <thead>
            <tr>
              <th>Produto</th>
              <th className="num">Itens na fórmula</th>
              <th className="num">Valor cadastrado</th>
              <th />
            </tr>
          </thead>

          <tbody>
            {produtos.map((produto) => (
              <tr key={produto.id}>
                <td>{produto.nome}</td>
                <td className="num">{produto.quantidadeItens}</td>
                <td className="num">
                  {formatarMoeda(produto.valorTotal)}
                </td>

                <td className="acao">
                  <Link to={`/produtos/${produto.id}`}>
                    Ver detalhes
                  </Link>
                  {' | '}
                  <Link to={`/produtos/${produto.id}/editar`}>
                    Editar
                  </Link>
                  {' | '}
                  <button
                    type="button"
                    className="botao-link perigo"
                    onClick={() =>
                      setProdutoSelecionado({
                        id: produto.id,
                        nome: produto.nome,
                      })
                    }
                  >
                    Excluir
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {produtoSelecionado && (
        <div className="modal-fundo">
          <div
            className="modal-exclusao"
            role="dialog"
            aria-modal="true"
            aria-labelledby="titulo-exclusao"
            aria-describedby="descricao-exclusao"
          >
            <div className="modal-icone">!</div>

            <h2 id="titulo-exclusao">Excluir produto?</h2>

            <p id="descricao-exclusao">
              Você está prestes a excluir o produto:
            </p>

            <strong className="nome-produto-exclusao">
              {produtoSelecionado.nome}
            </strong>

            <p className="modal-aviso">
              Essa ação não poderá ser desfeita. Deseja realmente continuar?
            </p>

            <div className="modal-acoes">
              <button
                type="button"
                className="botao botao-secundario"
                onClick={() => setProdutoSelecionado(null)}
                disabled={excluindo}
              >
                Cancelar
              </button>

              <button
                type="button"
                className="botao botao-excluir"
                onClick={excluirProduto}
                disabled={excluindo}
              >
                {excluindo ? 'Excluindo...' : 'Sim, excluir produto'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
