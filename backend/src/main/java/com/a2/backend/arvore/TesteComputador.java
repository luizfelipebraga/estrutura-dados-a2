package com.a2.backend.arvore;

import java.util.Arrays;
import java.util.List;

public class TesteComputador {

    public static void main(String[] args) {

        List<ItemFormula> itens = Arrays.asList(
            new ItemFormula("Computador", null, 1, 2500.00),
            new ItemFormula("Gabinete", "Computador", 1, 800.00),
            new ItemFormula("Placa-Mãe", "Gabinete", 1, 500.00),
            new ItemFormula("CPU", "Placa-Mãe", 1, 1000.00),
            new ItemFormula("Memória RAM", "Placa-Mãe", 2, 200.00),
            new ItemFormula("Chipset", "Placa-Mãe", 1, 150.00),
            new ItemFormula("Fonte de Alimentação", "Gabinete", 1, 300.00),
            new ItemFormula("Armazenamento", "Gabinete", 1, 0.00),
            new ItemFormula("HDD", "Armazenamento", 1, 250.00),
            new ItemFormula("SSD", "Armazenamento", 1, 400.00),
            new ItemFormula("Monitor", "Computador", 1, 900.00),
            new ItemFormula("Tela LCD", "Monitor", 1, 600.00),
            new ItemFormula("Carcaça", "Monitor", 1, 100.00),
            new ItemFormula("Periféricos", "Computador", 1, 0.00),
            new ItemFormula("Teclado", "Periféricos", 1, 100.00),
            new ItemFormula("Mouse", "Periféricos", 1, 80.00)
        );

        TreeNode raiz = ArvoreGenerica.buildTree(itens);

        System.out.println("Árvore da fórmula do computador:");
        ArvoreGenerica.printTree(raiz, 0);

        double total = ArvoreGenerica.calcularTotal(raiz);
        System.out.printf("\nValor total do computador: R$ %.2f\n", total);
    }
}
