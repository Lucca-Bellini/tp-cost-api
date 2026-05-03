package com.transformador.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstoqueResponseDto {
    private boolean sucesso;
    private String mensagem;
}
