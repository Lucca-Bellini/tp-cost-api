package com.transformador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InsumoRequestDto {

    @NotBlank(message = "ID do insumo é obrigatório")
    private String idInsumo; // ex: "INS-046"

    @NotNull(message = "Quantidade é obrigatória")
    private BigDecimal quantidade;
}