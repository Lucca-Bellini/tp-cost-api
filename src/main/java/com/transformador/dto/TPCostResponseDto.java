package com.transformador.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TPCostResponseDto {

    private boolean sucesso;
    private String mensagem;
    private BigDecimal custoTotal; // valor final do custo de fabricação de uma unidade de dado TP
    private BigDecimal custoMaoDeObra; // custo simplificado de mão de obra baseado na massa do TP
    private List<MaterialConsumo> detalhamentoMateriais;
}