package com.huertos.repository;

import com.huertos.model.ActividadComunidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActividadComunidadRepository extends JpaRepository<ActividadComunidad, Long> {

    boolean existsByComunidadIdAndNombreIgnoreCase(Long comunidadId, String nombre);

    List<ActividadComunidad> findByComunidadIdOrderByNombreAsc(Long comunidadId);
}