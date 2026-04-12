package com.transformador.repository;

import com.transformador.model.Nucleo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NucleoRepository extends JpaRepository<Nucleo, String> {

    /**
     * Busca núcleo pelo tipo (ex: "15kV FF")
     */
    Optional<Nucleo> findByTipo(String tipo);
}