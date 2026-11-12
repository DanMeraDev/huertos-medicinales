package com.huertos.repository;

import com.huertos.model.Comunidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ComunidadRepository extends JpaRepository<Comunidad, Long> {

    boolean existsByNombre(String nombre);

    Optional<Comunidad> findByResponsableId(Long responsableId);

    boolean existsByNombreAndIdNot(String nombre, Long id);

    @Query("SELECT c FROM Comunidad c WHERE c.activa = true ORDER BY c.nombre ASC")
    List<Comunidad> findAllActivasOrdenadas();
}
