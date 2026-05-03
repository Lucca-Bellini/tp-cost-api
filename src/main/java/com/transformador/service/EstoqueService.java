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
     * Verifica se há estoque suficiente para todos os itens e, em caso positivo,
     * debita.
     * Se qualquer item falhar, nenhum débito é efetivado.
     *
     * @param consumos Lista de materiais com suas quantidades a serem debitadas
     * @throws RuntimeException             se produto não encontrado
     * @throws EstoqueInsuficienteException se estoque insuficiente
     */
    public void validarEDebitar(List<InsumoRequestDto> itens) {
        // 1. Validação de estoque para todos os itens
        for (InsumoRequestDto item : itens) {
            Produto produto = produtoRepository.findByCodigo(item.getIdInsumo())
                    .orElseThrow(
                            () -> new EstoqueInsuficienteException("Produto não encontrado: " + item.getIdInsumo()));
            BigDecimal estoqueAtual = produto.getEstoque() != null ? produto.getEstoque() : BigDecimal.ZERO;
            if (estoqueAtual.compareTo(item.getQuantidade()) < 0) {
                throw new EstoqueInsuficienteException(String.format(
                        "Estoque insuficiente para %s (%s): disponível = %.3f, necessário = %.3f",
                        produto.getNome(), item.getIdInsumo(), estoqueAtual, item.getQuantidade()));
            }
        }

        // 2. Débito (apenas após validação bem‑sucedida)
        for (InsumoRequestDto item : itens) {
            Produto produto = produtoRepository.findByCodigo(item.getIdInsumo()).get(); // existe
            BigDecimal novoEstoque = produto.getEstoque().subtract(item.getQuantidade());
            produto.setEstoque(novoEstoque);
            produtoRepository.save(produto);
            log.info("Estoque debitado: {} - nova quantidade = {}", item.getIdInsumo(), novoEstoque);
        }
    }
}