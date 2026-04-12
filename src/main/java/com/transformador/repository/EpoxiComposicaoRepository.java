package com.transformador.repository;

import com.transformador.model.EpoxiComposicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpoxiComposicaoRepository extends JpaRepository<EpoxiComposicao, Long> {
    // O método findAll() já está disponível pela herança.
    // Se precisar de consultas personalizadas, adicione aqui.
}