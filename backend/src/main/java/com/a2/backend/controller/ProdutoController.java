package com.a2.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.a2.backend.arvore.Necessidade;
import com.a2.backend.arvore.NoExplosao;
import com.a2.backend.dto.AtualizarValorRequest;
import com.a2.backend.dto.NovoProdutoRequest;
import com.a2.backend.dto.ProdutoResumo;
import com.a2.backend.dto.ResultadoImplosao;
import com.a2.backend.service.ProdutoService;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<ProdutoResumo> listar() {
        return produtoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResumo criar(@RequestBody NovoProdutoRequest request) {
        return produtoService.criar(request);
    }

    @PutMapping("/{id}")
    public ProdutoResumo atualizar(@PathVariable Integer id, @RequestBody NovoProdutoRequest request) {
        return produtoService.atualizar(id, request);
    }

    @GetMapping("/{id}/explosao")
    public NoExplosao explosao(@PathVariable Integer id) {
        return produtoService.explosao(id);
    }

    @GetMapping("/{id}/necessidade")
    public Necessidade necessidade(@PathVariable Integer id, @RequestParam int quantidade) {
        return produtoService.necessidade(id, quantidade);
    }

    @PatchMapping("/{id}/itens/{itemId}")
    public NoExplosao atualizarValor(@PathVariable Integer id, @PathVariable Integer itemId,
            @RequestBody AtualizarValorRequest request) {
        return produtoService.atualizarValor(id, itemId, request.valor());
    }

    @PostMapping("/{id}/implosao")
    public ResultadoImplosao implodir(@PathVariable Integer id) {
        return produtoService.implodir(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer id) {
        produtoService.excluir(id);
    }
}
