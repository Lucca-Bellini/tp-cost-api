package com.transformador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "carretel")
@Data
public class Carretel {

    @Id
    @Column(name = "tamanho", length = 10)
    private String tamanho;

    @Column(name = "circunferencia_mm", precision = 10, scale = 2)
    private BigDecimal circunferenciaMm;

    @Column(name = "epoxi_carretel_kg", precision = 10, scale = 2)
    private BigDecimal epoxiCarretelKg;

    @Column(name = "epoxi_capa_kg", precision = 10, scale = 2)
    private BigDecimal epoxiCapaKg;

    @Column(name = "primario_papel_largura_mm", precision = 10, scale = 2)
    private BigDecimal primarioPapelLarguraMm;

    @Column(name = "primario_largura_espira_mm", precision = 10, scale = 2)
    private BigDecimal primarioLarguraEspiraMm;

    @Column(name = "fita_latao_massa_kg", precision = 10, scale = 3)
    private BigDecimal fitaLataoMassaKg;

    @Column(name = "cano_altura_mm", precision = 10, scale = 2)
    private BigDecimal canoAlturaMm;

    @Column(name = "cano_circunferencia_mm", precision = 10, scale = 2)
    private BigDecimal canoCircunferenciaMm;

    @Column(name = "secundario_largura_espira_mm", precision = 10, scale = 2)
    private BigDecimal secundarioLarguraEspiraMm;
}