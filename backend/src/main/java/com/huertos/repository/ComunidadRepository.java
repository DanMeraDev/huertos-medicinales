package com.huertos.repository;

import com.huertos.model.Comunidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunidadRepository extends JpaRepository<Comunidad, Long> {

    boolean existsByNombre(String nombre);

    Optional<Comunidad> findByResponsableId(Long responsableId);

    boolean existsByNombreAndIdNot(String nombre, Long id);
}
