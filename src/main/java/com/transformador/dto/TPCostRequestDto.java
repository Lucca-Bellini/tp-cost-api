package com.transformador.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TPCostRequestDto {

    // Campos obrigatórios do TP
    @NotNull(message = "Tensão máxima é obrigatória")
    @Positive(message = "Tensão máxima deve ser positiva")
    private Integer tensaoMaxima; // 15, 24, 36

    @NotNull(message = "AWG primário é obrigatório")
    @Positive(message = "AWG primário deve ser positivo")
    private Integer awgPrimario;

    @NotNull(message = "Espiras primário é obrigatório")
    @Positive(message = "Espiras primário deve ser positivo")
    private Integer espirasPrimario;

    @NotNull(message = "AWG secundário é obrigatório")
    @Positive(message = "AWG secundário deve ser positivo")
    private Integer awgSecundario;

    @NotNull(message = "Espiras secundário é obrigatório")
    @Positive(message = "Espiras secundário deve ser positivo")
    private Integer espirasSecundario;

    @NotBlank(message = "Tipo de ligação é obrigatório")
    @Pattern(regexp = "^(Fase-Terra|Fase-Fase)$", message = "Tipo de ligação deve ser Fase-Terra ou Fase-Fase")
    private String tipoLigacao; // "Fase-Terra" ou "Fase-Fase"

    @NotNull(message = "Número de isoladores é obrigatório")
    @Min(value = 0, message = "Número de isoladores não pode ser negativo")
    private Integer numeroIsoladores; // 0 para tipoUso = "Interno"; >0 para tipoUso = "Externo"

    @NotNull(message = "Massa de encapsulamento do molde é obrigatória")
    @Positive(message = "Massa de encapsulamento deve ser positiva")
    private BigDecimal massaEncapsulamentoMolde; // kg

    @NotBlank(message = "Carretel é obrigatório")
    @Pattern(regexp = "^(P|M|G)$", message = "Carretel deve ser P, M ou G")
    private String carretel; // P, M, G

    // Lista de insumos simples (parafusos, insertos, etc.)
    private List<InsumoRequestDto> insumos;
}