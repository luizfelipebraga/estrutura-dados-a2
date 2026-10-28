package com.a2.backend.arvore;

import java.util.Arrays;
import java.util.List;

public class TesteCaneta {

    public static void main(String[] args) {

        List<ItemFormula> itens = Arrays.asList(
            new ItemFormula("Caneta", null, 1, 2.50),
            new ItemFormula("Tinta", "Caneta", 1, 0.80),
            new ItemFormula("Pigmento", "Tinta", 1, 0.30),
            new ItemFormula("Solvente", "Tinta", 1, 0.50),
            new ItemFormula("Corpo", "Caneta", 1, 1.00),
            new ItemFormula("Plástico 1", "Corpo", 1, 0.70),
            new ItemFormula("Clip", "Corpo", 1, 0.30),
            new ItemFormula("Tampa", "Caneta", 1, 0.70),
            new ItemFormula("Plástico 2", "Tampa", 1, 0.50),
            new ItemFormula("Vedação", "Tampa", 1, 0.20)
        );

        TreeNode raiz = ArvoreGenerica.buildTree(itens);

        // Exibir árvore
        System.out.println("Árvore da fórmula da caneta:");
        ArvoreGenerica.printTree(raiz, 0);

        // Calcular valor unitário
        double total = ArvoreGenerica.calcularTotal(raiz);
        System.out.printf("\nValor total da caneta: R$ %.2f\n", total);

        // Calcular custo de produção
        int quantidade = 10;

        double custoProducao = ArvoreGenerica.calcularProducao(raiz, quantidade);

        System.out.printf(
            "Custo para produzir %d canetas: R$ %.2f\n",
            quantidade,
            custoProducao
        );

        // Calcular quantidade de componentes
        System.out.println("\nComponentes para produzir 10 canetas:");
        ArvoreGenerica.calcularComponentes(raiz, quantidade);

        // Atualizar preço do Pigmento
        System.out.println("\nAtualizando o preço do Pigmento...");

        ArvoreGenerica.atualizarPreco(raiz, "Pigmento", 0.50);

        // Recalcular custo após atualização
        double novoTotal = ArvoreGenerica.calcularTotal(raiz);

        System.out.printf("Novo custo da caneta: R$ %.2f\n", novoTotal);
        
        double custoImplosao = ArvoreGenerica.implosao(raiz);

        System.out.printf(
            "Custo após implosão: R$ %.2f\n",
            custoImplosao
        );
    }
}
