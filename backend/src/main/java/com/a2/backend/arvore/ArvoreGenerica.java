package com.a2.backend.arvore;

import java.util.List;
import java.util.ArrayList;

public class ArvoreGenerica {

    public static TreeNode buildTree(List<ItemFormula> itens) {
        if (itens == null || itens.isEmpty()) {
            return null;
        }

        TreeNode raiz = null;

        for (ItemFormula item : itens) {
            if (item.getPai() == null) {
                raiz = new TreeNode(item);
                break;
            }
        }

        if (raiz == null) {
            return null;
        }

        adicionarFilhos(raiz, itens);
        return raiz;
    }

    private static void adicionarFilhos(TreeNode pai, List<ItemFormula> itens) {
        for (ItemFormula item : itens) {
            if (pai.getItem().getNome().equals(item.getPai())) {
                TreeNode filho = new TreeNode(item);
                pai.adicionarFilho(filho);
                adicionarFilhos(filho, itens);
            }
        }
    }

    public static void printTree(TreeNode no, int nivel) {
        if (no == null) {
            return;
        }

        System.out.println("  ".repeat(nivel) + no.getItem().getNome());

        for (TreeNode filho : no.getFilhos()) {
            printTree(filho, nivel + 1);
        }
    }

    public static double calcularTotal(TreeNode no) {
        if (no == null) {
            return 0;
        }

        if (no.getFilhos().isEmpty()) {
            return no.getItem().getValor();
        }

        double total = 0;

        for (TreeNode filho : no.getFilhos()) {
            total += calcularTotal(filho) * filho.getItem().getQuantidade();
        }

        return total;
    }

    public static double calcularProducao(TreeNode raiz, int quantidade) {
        if (raiz == null || quantidade <= 0) {
            return 0;
        }

        return calcularTotal(raiz) * quantidade;
    }

    public static void calcularComponentes(TreeNode no, int quantidade) {
        if (no == null || quantidade <= 0) {
            return;
        }

        for (TreeNode filho : no.getFilhos()) {
            int total = quantidade * filho.getItem().getQuantidade();

            System.out.println(filho.getItem().getNome() + ": " + total);

            calcularComponentes(filho, total);
        }
    }

    public static boolean atualizarPreco(TreeNode no, String nome, double novoValor) {
        if (no == null || novoValor < 0) {
            return false;
        }

        if (no.getItem().getNome().equals(nome)) {
            no.getItem().setValor(novoValor);
            return true;
        }

        for (TreeNode filho : no.getFilhos()) {
            if (atualizarPreco(filho, nome, novoValor)) {
                return true;
            }
        }

        return false;
    }

    public static double implosao(TreeNode no) {
        if (no == null) {
            return 0;
        }

        if (no.getFilhos().isEmpty()) {
            return no.getItem().getValor();
        }

        double total = 0;

        for (TreeNode filho : no.getFilhos()) {
            total += implosao(filho) * filho.getItem().getQuantidade();
        }

        no.getItem().setValor(total);
        return total;
    }

    public static List<String> listarComponentes(TreeNode no, int quantidade) {
        List<String> componentes = new ArrayList<>();

        if (no == null || quantidade <= 0) {
            return componentes;
        }

        for (TreeNode filho : no.getFilhos()) {
            int total = quantidade * filho.getItem().getQuantidade();

            componentes.add(filho.getItem().getNome() + ": " + total);
            componentes.addAll(listarComponentes(filho, total));
        }

        return componentes;
    }
}
