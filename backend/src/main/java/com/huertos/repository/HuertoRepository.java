package com.huertos.repository;

import com.huertos.model.Huerto;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HuertoRepository extends JpaRepository<Huerto, Long> {

    Optional<Huerto> findByComunidadResponsableId(Long responsableId);
    Optional<Huerto> findByComunidadId(Long comunidadId);
}
