package com.transformador.service;

import com.transformador.dto.InsumoRequestDto;
import com.transformador.exception.EstoqueInsuficienteException;
import com.transformador.model.Produto;
import com.transformador.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstoqueService {

    private final ProdutoRepository produtoRepository;

    /**
     * Verifica se há estoque suficiente para todos os insumos e, em caso positivo,
     * debita.
     * Se qualquer insumo falhar, nenhum débito é efetivado.
     *
     * @param consumos Lista de materiais com suas quantidades a serem debitadas
     * @throws RuntimeException             se produto não encontrado
     * @throws EstoqueInsuficienteException se estoque insuficiente
     */
    public void validarEDebitar(List<InsumoRequestDto> insumos) {
        // 1. Validação de estoque para todos os insumos
        for (InsumoRequestDto insumo : insumos) {
            Produto produto = produtoRepository.findByCodigo(insumo.getIdInsumo())
                    .orElseThrow(
                            () -> new EstoqueInsuficienteException("Produto não encontrado: " + insumo.getIdInsumo()));
            BigDecimal estoqueAtual = produto.getEstoque() != null ? produto.getEstoque() : BigDecimal.ZERO;
            if (estoqueAtual.compareTo(insumo.getQuantidade()) < 0) {
                throw new EstoqueInsuficienteException(String.format(
                        "Estoque insuficiente para %s (%s): disponível = %.3f, necessário = %.3f",
                        produto.getNome(), insumo.getIdInsumo(), estoqueAtual, insumo.getQuantidade()));
            }
        }

        // 2. Débito (apenas após validação bem‑sucedida)
        for (InsumoRequestDto insumo : insumos) {
            Produto produto = produtoRepository.findByCodigo(insumo.getIdInsumo()).get();
            BigDecimal novoEstoque = produto.getEstoque().subtract(insumo.getQuantidade());
            produto.setEstoque(novoEstoque);
            produtoRepository.save(produto);
            log.info("Estoque debitado: {} - nova quantidade = {}", insumo.getIdInsumo(), novoEstoque);
        }
    }
}