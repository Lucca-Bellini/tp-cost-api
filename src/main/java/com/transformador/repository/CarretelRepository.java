package com.transformador.repository;

import com.transformador.model.Carretel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CarretelRepository extends JpaRepository<Carretel, String> {

    /**
     * Busca carretel pelo tamanho ("P", "M", "G")
     */
    Optional<Carretel> findByTamanho(String tamanho);
}