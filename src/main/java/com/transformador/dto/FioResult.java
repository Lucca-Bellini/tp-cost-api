package com.transformador.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class FioResult {
    private BigDecimal custo;
    private BigDecimal massa;
    private BigDecimal numeroCamadas;
    private BigDecimal raioMedioMm;
}