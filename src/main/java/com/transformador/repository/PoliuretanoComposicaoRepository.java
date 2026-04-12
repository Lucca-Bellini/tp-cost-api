package com.transformador.repository;

import com.transformador.model.PoliuretanoComposicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliuretanoComposicaoRepository extends JpaRepository<PoliuretanoComposicao, Long> {
    // O método findAll() já está disponível pela herança.
    // Se precisar de consultas personalizadas, adicione aqui.
}