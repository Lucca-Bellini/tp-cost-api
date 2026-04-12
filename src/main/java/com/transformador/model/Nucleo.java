package com.transformador.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "nucleo")
@Data
public class Nucleo {

    @Id
    @Column(name = "tipo", length = 20)
    private String tipo;

    @Column(name = "massa_kg", precision = 10, scale = 2)
    private BigDecimal massaKg;

    @Column(name = "cinta_massa_kg", precision = 10, scale = 3)
    private BigDecimal cintaMassaKg;

    @Column(name = "cinta_metro", precision = 10, scale = 2)
    private BigDecimal cintaMetro;
}