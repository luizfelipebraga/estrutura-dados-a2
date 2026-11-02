package com.a2.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.a2.backend.arvore.AlteracaoValor;
import com.a2.backend.arvore.ComponenteNecessario;
import com.a2.backend.arvore.Necessidade;
import com.a2.backend.arvore.NoExplosao;
import com.a2.backend.dto.ItemFormulaRequest;
import com.a2.backend.dto.NovoProdutoRequest;
import com.a2.backend.dto.ProdutoResumo;
import com.a2.backend.dto.ResultadoImplosao;
import com.a2.backend.entity.Itemformula;
import com.a2.backend.entity.Produto;
import com.a2.backend.exception.RecursoNaoEncontradoException;
import com.a2.backend.repository.ItemFormulaRepository;
import com.a2.backend.repository.ProdutoRepository;


@Service
public class ProdutoService {

    private static final int ESCALA = 2;

    private final ProdutoRepository produtoRepository;
    private final ItemFormulaRepository itemRepository;

    public ProdutoService(ProdutoRepository produtoRepository, ItemFormulaRepository itemRepository) {
        this.produtoRepository = produtoRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResumo> listar() {
        return produtoRepository.findAll().stream().map(this::resumir).toList();
    }

    @Transactional
    public ProdutoResumo criar(NovoProdutoRequest req) {
        if (req == null || req.nome() == null || req.nome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome do produto");
        }
        if (req.itens() == null || req.itens().isEmpty()) {
            throw new IllegalArgumentException("A fórmula precisa ter ao menos um item");
        }
        List<Itemformula> itens = new ArrayList<>();
        List<String> pais = new ArrayList<>();
        for (ItemFormulaRequest r : req.itens()) {
            itens.add(criarItem(r));
            pais.add(r.pai() == null || r.pai().isBlank() ? null : r.pai().trim());
        }
        Set<String> nomesPai = pais.stream().filter(p -> p != null).collect(Collectors.toSet());
        Map<String, Itemformula> porNome = new HashMap<>();
        for (Itemformula item : itens) {
            if (nomesPai.contains(item.getNome()) && porNome.put(item.getNome(), item) != null) {
                throw new IllegalArgumentException(
                        "O item '" + item.getNome() + "' é usado como pai e seu nome se repete na fórmula");
            }
        }
        Itemformula raiz = null;
        for (int i = 0; i < itens.size(); i++) {
            Itemformula item = itens.get(i);
            String paiNome = pais.get(i);
            if (paiNome == null) {
                if (raiz != null) {
                    throw new IllegalArgumentException("A fórmula deve ter apenas um item raiz (sem pai)");
                }
                raiz = item;
            } else {
                Itemformula pai = porNome.get(paiNome);
                if (pai == null) {
                    throw new IllegalArgumentException("Pai '" + paiNome + "' não existe na fórmula");
                }
                pai.adicionarFilho(item);
            }
        }
        if (raiz == null) {
            throw new IllegalArgumentException("Informe o item raiz (sem pai)");
        }
        if (contar(raiz) != itens.size()) {
            throw new IllegalArgumentException("Há itens que não estão ligados à raiz (ciclo ou pai inválido)");
        }

        Produto produto = new Produto();
        produto.setNome(req.nome().trim());
        produto = produtoRepository.save(produto);
        for (Itemformula item : itens) {
            item.setProduto(produto);
        }
        itemRepository.save(raiz);

        return resumir(produto);
    }

    @Transactional
    public ProdutoResumo atualizar(Integer produtoId, NovoProdutoRequest req) {
        if (req == null || req.nome() == null || req.nome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome do produto");
        }
        if (req.itens() == null || req.itens().isEmpty()) {
            throw new IllegalArgumentException("A fórmula precisa ter ao menos um item");
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto " + produtoId + " não encontrado"));

        List<Itemformula> itens = new ArrayList<>();
        List<String> pais = new ArrayList<>();
        for (ItemFormulaRequest r : req.itens()) {
            itens.add(criarItem(r));
            pais.add(r.pai() == null || r.pai().isBlank() ? null : r.pai().trim());
        }

        Set<String> nomesPai = pais.stream().filter(p -> p != null).collect(Collectors.toSet());
        Map<String, Itemformula> porNome = new HashMap<>();
        for (Itemformula item : itens) {
            if (nomesPai.contains(item.getNome()) && porNome.put(item.getNome(), item) != null) {
                throw new IllegalArgumentException(
                        "O item '" + item.getNome() + "' é usado como pai e seu nome se repete na fórmula");
            }
        }

        Itemformula novaRaiz = null;
        for (int i = 0; i < itens.size(); i++) {
            Itemformula item = itens.get(i);
            String paiNome = pais.get(i);
            if (paiNome == null) {
                if (novaRaiz != null) {
                    throw new IllegalArgumentException("A fórmula deve ter apenas um item raiz (sem pai)");
                }
                novaRaiz = item;
            } else {
                Itemformula pai = porNome.get(paiNome);
                if (pai == null) {
                    throw new IllegalArgumentException("Pai '" + paiNome + "' não existe na fórmula");
                }
                pai.adicionarFilho(item);
            }
        }
        if (novaRaiz == null) {
            throw new IllegalArgumentException("Informe o item raiz (sem pai)");
        }
        if (contar(novaRaiz) != itens.size()) {
            throw new IllegalArgumentException("Há itens que não estão ligados à raiz (ciclo ou pai inválido)");
        }

        // Substitui a fórmula antiga pela nova dentro da mesma transação.
        itemRepository.findByProdutoIdAndPaiIsNull(produtoId).ifPresent(itemRepository::delete);
        itemRepository.flush();

        produto.setNome(req.nome().trim());
        for (Itemformula item : itens) {
            item.setProduto(produto);
        }
        itemRepository.save(novaRaiz);

        return resumir(produto);
    }

    @Transactional(readOnly = true)
    public NoExplosao explosao(Integer produtoId) {
        return paraExplosao(raizDe(produtoId));
    }

    @Transactional(readOnly = true)
    public Necessidade necessidade(Integer produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }
        Itemformula raiz = raizDe(produtoId);

        Map<String, Acumulado> acumulado = new LinkedHashMap<>();
        if (raiz.getFilhos().isEmpty()) {
            acumular(acumulado, raiz, quantidade);
        } else {
            coletarFolhas(raiz, quantidade, acumulado);
        }

        List<ComponenteNecessario> componentes = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Acumulado> e : acumulado.entrySet()) {
            Acumulado a = e.getValue();
          
            BigDecimal unitario = a.total.divide(BigDecimal.valueOf(a.quantidade), 4, RoundingMode.HALF_UP);
            componentes.add(new ComponenteNecessario(e.getKey(), a.quantidade,
                    unitario.doubleValue(), a.total.doubleValue()));
            total = total.add(a.total);
        }
        return new Necessidade(quantidade, componentes, total.doubleValue());
    }


    @Transactional
    public NoExplosao atualizarValor(Integer produtoId, Integer itemId, Double valor) {
        if (valor == null || valor < 0) {
            throw new IllegalArgumentException("Informe um valor maior ou igual a zero");
        }
        Itemformula item = itemRepository.findById(itemId)
                .filter(i -> i.getProduto().getId().equals(produtoId))
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Item " + itemId + " não encontrado no produto " + produtoId));

        item.setValor(moeda(valor));
        return paraExplosao(raizDe(produtoId));
    }

    @Transactional
    public ResultadoImplosao implodir(Integer produtoId) {
        Itemformula raiz = raizDe(produtoId);
        List<AlteracaoValor> alteracoes = new ArrayList<>();
        implodir(raiz, alteracoes);
        return new ResultadoImplosao(paraExplosao(raiz), alteracoes);
    }

  
    @Transactional
    public void excluir(Integer produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + produtoId + " não encontrado"));
        // apagar a raiz apaga a árvore inteira (cascade ALL + orphanRemoval)
        itemRepository.findByProdutoIdAndPaiIsNull(produtoId).ifPresent(itemRepository::delete);
        produtoRepository.delete(produto);
    }

  
    private BigDecimal custo(Itemformula no) {
        if (no.getFilhos().isEmpty()) {
            return no.getValor();
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Itemformula filho : no.getFilhos()) {
            total = total.add(custo(filho).multiply(BigDecimal.valueOf(filho.getQuantidade())));
        }
        return total;
    }


    private BigDecimal implodir(Itemformula no, List<AlteracaoValor> alteracoes) {
        if (no.getFilhos().isEmpty()) {
            return no.getValor();
        }
        BigDecimal soma = BigDecimal.ZERO;
        for (Itemformula filho : no.getFilhos()) {
            soma = soma.add(implodir(filho, alteracoes).multiply(BigDecimal.valueOf(filho.getQuantidade())));
        }
        soma = soma.setScale(ESCALA, RoundingMode.HALF_UP);
        if (soma.compareTo(no.getValor()) != 0) {
            alteracoes.add(new AlteracaoValor(no.getId(), no.getNome(),
                    no.getValor().doubleValue(), soma.doubleValue()));
            no.setValor(soma); // dirty checking grava no commit
        }
        return soma;
    }


    private NoExplosao paraExplosao(Itemformula no) {
        List<NoExplosao> filhos = no.getFilhos().stream().map(this::paraExplosao).toList();
        BigDecimal unitario = no.getValor();
        BigDecimal total = unitario.multiply(BigDecimal.valueOf(no.getQuantidade()));
        return new NoExplosao(no.getId(), no.getNome(), no.getQuantidade(),
                unitario.doubleValue(), total.doubleValue(), filhos);
    }

    
    private void coletarFolhas(Itemformula no, int quantidadePai, Map<String, Acumulado> acumulado) {
        for (Itemformula filho : no.getFilhos()) {
            int quantidade = quantidadePai * filho.getQuantidade();
            if (filho.getFilhos().isEmpty()) {
                acumular(acumulado, filho, quantidade);
            } else {
                coletarFolhas(filho, quantidade, acumulado);
            }
        }
    }

    private void acumular(Map<String, Acumulado> acumulado, Itemformula folha, int quantidade) {
        Acumulado a = acumulado.computeIfAbsent(folha.getNome(), n -> new Acumulado());
        a.quantidade += quantidade;
        a.total = a.total.add(folha.getValor().multiply(BigDecimal.valueOf(quantidade)));
    }

    private int contar(Itemformula no) {
        int n = 1;
        for (Itemformula filho : no.getFilhos()) {
            n += contar(filho);
        }
        return n;
    }

  

    private Itemformula raizDe(Integer produtoId) {
        if (!produtoRepository.existsById(produtoId)) {
            throw new RecursoNaoEncontradoException("Produto " + produtoId + " não encontrado");
        }
        return itemRepository.findByProdutoIdAndPaiIsNull(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto " + produtoId + " não possui item raiz"));
    }

    private ProdutoResumo resumir(Produto p) {
        Itemformula raiz = raizDe(p.getId());
        return new ProdutoResumo(p.getId(), p.getNome(),
                itemRepository.countByProdutoId(p.getId()), raiz.getValor().doubleValue());
    }

    private Itemformula criarItem(ItemFormulaRequest r) {
        if (r == null || r.nome() == null || r.nome().isBlank()) {
            throw new IllegalArgumentException("Todo item precisa de um nome");
        }
        if (r.quantidade() == null || r.quantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade de '" + r.nome() + "' deve ser maior que zero");
        }
        if (r.valor() == null || r.valor() < 0) {
            throw new IllegalArgumentException("O valor de '" + r.nome() + "' deve ser maior ou igual a zero");
        }
        Itemformula item = new Itemformula();
        item.setNome(r.nome().trim());
        item.setQuantidade(r.quantidade());
        item.setValor(moeda(r.valor()));
        return item;
    }

    private BigDecimal moeda(double valor) {
        return BigDecimal.valueOf(valor).setScale(ESCALA, RoundingMode.HALF_UP);
    }

    private static class Acumulado {
        int quantidade = 0;
        BigDecimal total = BigDecimal.ZERO;
    }
}
