package com.a2.backend.arvore;

import java.util.List;

public record Necessidade(int quantidade, List<ComponenteNecessario> componentes, double valorTotal) {
}
