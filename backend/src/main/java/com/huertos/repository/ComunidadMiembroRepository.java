package com.huertos.repository;

import com.huertos.model.ComunidadMiembro;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComunidadMiembroRepository extends JpaRepository<ComunidadMiembro, Long> {

    boolean existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(Long comunidadId, Long miembroId);

    Optional<ComunidadMiembro> findByComunidadIdAndMiembroId(Long comunidadId, Long miembroId);

    long countByComunidadIdAndActivoEnComunidadTrue(Long comunidadId);
}
