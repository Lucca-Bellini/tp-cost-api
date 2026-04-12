package com.transformador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "poliuretano_composicao")
@Data
public class PoliuretanoComposicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String componente;

    @Column(name = "quantidade_kg", precision = 10, scale = 3)
    private BigDecimal quantidadeKg;

    @ManyToOne
    @JoinColumn(name = "material_codigo", referencedColumnName = "codigo")
    private Produto material;
}