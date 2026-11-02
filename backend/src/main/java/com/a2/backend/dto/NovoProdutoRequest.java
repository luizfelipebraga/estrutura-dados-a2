package com.a2.backend.dto;

import java.util.List;

public record NovoProdutoRequest(String nome, List<ItemFormulaRequest> itens) {
}
    