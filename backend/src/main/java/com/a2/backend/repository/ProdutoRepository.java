package com.a2.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.a2.backend.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
}
