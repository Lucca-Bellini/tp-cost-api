package com.transformador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "produto_cobre")
@Data
public class ProdutoCobre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_produto", length = 20, nullable = false, unique = true)
    private String codigoProduto;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_produto", referencedColumnName = "codigo", insertable = false, updatable = false)
    private Produto produto;

    @Column(name = "awg")
    private Integer awg;

    @Column(name = "diametro", precision = 10, scale = 4)
    private BigDecimal diametro;

    @Column(name = "secao", precision = 10, scale = 4)
    private BigDecimal secao;

    @Column(name = "espiras_por_cm", precision = 10, scale = 2)
    private BigDecimal espirasPorCm;

    @Column(name = "kg_por_km", precision = 10, scale = 2)
    private BigDecimal kgPorKm;

    @Column(name = "resistencia", precision = 10, scale = 2)
    private BigDecimal resistencia;

    @Column(name = "capacidade", precision = 10, scale = 2)
    private BigDecimal capacidade;
}