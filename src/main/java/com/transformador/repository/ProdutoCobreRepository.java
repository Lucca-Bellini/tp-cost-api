package com.transformador.repository;

import com.transformador.model.ProdutoCobre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProdutoCobreRepository extends JpaRepository<ProdutoCobre, String> {

    Optional<ProdutoCobre> findByCodigoProduto(String codigoProduto);

    @Query("SELECT pc FROM ProdutoCobre pc WHERE pc.awg = :awg")
    Optional<ProdutoCobre> findByAwg(@Param("awg") Integer awg);
}