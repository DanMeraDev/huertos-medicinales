package com.huertos.repository;

import com.huertos.model.Comunidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunidadRepository extends JpaRepository<Comunidad, Long> {

    boolean existsByNombre(String nombre);
}
