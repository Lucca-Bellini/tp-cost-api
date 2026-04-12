package com.transformador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "epoxi_composicao")
@Data
public class EpoxiComposicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "componente", length = 50, nullable = false)
    private String componente;

    @Column(name = "fator", precision = 10, scale = 6, nullable = false)
    private BigDecimal fator;

    @ManyToOne
    @JoinColumn(name = "material_codigo", referencedColumnName = "codigo")
    private Produto material;
}