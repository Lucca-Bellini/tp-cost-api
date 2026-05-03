package com.transformador.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstoqueResponseDto {
    private boolean sucesso;
    private List<String> detalhes;
}
