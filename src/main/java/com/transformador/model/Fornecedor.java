package com.transformador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "fornecedor")
@Data
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "ativo")
    private Boolean ativo;

    @OneToMany(mappedBy = "fornecedor")
    private List<ProdutoFornecedor> produtosFornecedor;
}