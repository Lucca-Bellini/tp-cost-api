package com.transformador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "produto")
@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "tipo_material", length = 50)
    private String tipoMaterial;

    @Column(name = "unidade_medida", length = 10)
    private String unidadeMedida;

    @Column(name = "disponibilidade")
    private Boolean disponibilidade;

    @Column(name = "estoque", precision = 12, scale = 6)
    private BigDecimal estoque;

    @Column(name = "massa_por_unidade", precision = 10, scale = 2)
    private BigDecimal massaPorUnidade;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProdutoFornecedor> precos;

    @OneToOne(mappedBy = "produto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProdutoCobre especificacoesCobre;
}