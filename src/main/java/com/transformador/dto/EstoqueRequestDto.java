package com.transformador.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class EstoqueRequestDto {
    @NotEmpty(message = "Lista de itens não pode ser vazia")
    private List<@Valid InsumoRequestDto> insumos;
}