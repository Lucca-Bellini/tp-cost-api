package com.transformador.repository;

import com.transformador.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /**
     * Busca produto pelo código (ex: "INS-046")
     */
    Optional<Produto> findByCodigo(String codigo);
}