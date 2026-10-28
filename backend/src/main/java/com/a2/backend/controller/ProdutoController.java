package com.a2.backend.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import com.a2.backend.service.ProdutoService;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.a2.backend.arvore.ItemFormula;
import com.a2.backend.arvore.TreeNode;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @PostMapping("/calcular")
    public double calcularCusto(@RequestBody List<ItemFormula> itens) {

        TreeNode raiz = produtoService.montarArvore(itens);

        return produtoService.calcularCusto(raiz);
    }

}
