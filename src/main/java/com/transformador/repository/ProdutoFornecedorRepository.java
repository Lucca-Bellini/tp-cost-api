package com.transformador.repository;

import com.transformador.model.ProdutoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProdutoFornecedorRepository extends JpaRepository<ProdutoFornecedor, Long> {

        /**
         * Retorna o menor preço ativo de um produto (considerando fornecedores ativos)
         * 
         * @param produtoId ID do produto
         * @return menor valor encontrado, ou vazio se nenhum ativo
         */
        @Query("SELECT MIN(pf.valor) FROM ProdutoFornecedor pf " +
                        "WHERE pf.produto.id = :produtoId AND pf.ativo = true")
        Optional<BigDecimal> findLowestPriceByProdutoId(@Param("produtoId") Long produtoId);

        /**
         * Retorna o menor preço ativo de um produto através do seu código
         * 
         * @param codigoProduto código do produto (ex: "INS-046")
         * @return menor valor encontrado, ou vazio
         */
        @Query("SELECT MIN(pf.valor) FROM ProdutoFornecedor pf " +
                        "WHERE pf.produto.codigo = :codigoProduto AND pf.ativo = true")
        Optional<BigDecimal> findLowestPriceByProdutoCodigo(@Param("codigoProduto") String codigoProduto);
}