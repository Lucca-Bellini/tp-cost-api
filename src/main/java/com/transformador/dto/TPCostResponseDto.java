package com.transformador.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class TPCostResponseDto {

    private boolean sucesso;
    private String mensagem;
    private BigDecimal custoTotal; // valor final do custo de fabricação de uma unidade de dado TP

    // Opcional: detalhes podem ser adicionados futuramente, como um relatorio de
    // custos destrinchado
    // private BigDecimal custoMateriaPrima;
    // private BigDecimal custoMaoDeObra;
    // etc.
}