package com.a2.backend.dto;

import java.util.List;

import com.a2.backend.arvore.AlteracaoValor;
import com.a2.backend.arvore.NoExplosao;

public record ResultadoImplosao(NoExplosao arvore, List<AlteracaoValor> alteracoes) {
}
