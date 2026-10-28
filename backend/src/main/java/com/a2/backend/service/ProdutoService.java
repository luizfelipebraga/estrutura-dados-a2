package com.a2.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.a2.backend.arvore.ArvoreGenerica;
import com.a2.backend.arvore.ItemFormula;
import com.a2.backend.arvore.TreeNode;

@Service
public class ProdutoService {

    public TreeNode montarArvore(List<ItemFormula> itens) {
        return ArvoreGenerica.buildTree(itens);
    }

    public double calcularCusto(TreeNode raiz) {
        return ArvoreGenerica.calcularTotal(raiz);
    }

    public double calcularProducao(TreeNode raiz, int quantidade) {
        return ArvoreGenerica.calcularProducao(raiz, quantidade);
    }

    public List<String> listarComponentes(TreeNode raiz, int quantidade) {
        return ArvoreGenerica.listarComponentes(raiz, quantidade);
    }

    public boolean atualizarPreco(TreeNode raiz, String nome, double valor) {
        return ArvoreGenerica.atualizarPreco(raiz, nome, valor);
    }

    public double implodir(TreeNode raiz) {
        return ArvoreGenerica.implosao(raiz);
    }
}
