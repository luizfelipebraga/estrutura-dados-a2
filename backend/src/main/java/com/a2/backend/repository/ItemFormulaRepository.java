package com.a2.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.a2.backend.entity.Itemformula;

public interface ItemFormulaRepository extends JpaRepository<Itemformula, Integer> {

    Optional<Itemformula> findByProdutoIdAndPaiIsNull(Integer produtoId);

    long countByProdutoId(Integer produtoId);
}
