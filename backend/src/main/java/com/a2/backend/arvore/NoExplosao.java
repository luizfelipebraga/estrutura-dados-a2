package com.a2.backend.arvore;

import java.util.List;


public record NoExplosao(
        Integer id,
        String nome,
        int quantidade,
        double valorUnitario,
        double valorTotal,
        List<NoExplosao> filhos) {
}
