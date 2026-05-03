package com.transformador.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
public class MaterialConsumo {
    private String codigo;

    private String nome;

    private BigDecimal quantidade; // quantidade na unidade comercial original (peças, kg, m)

    @JsonIgnore
    private BigDecimal massaKg; // peso em kg (quantidade * massaPorUnidade)

    private BigDecimal custo;
}