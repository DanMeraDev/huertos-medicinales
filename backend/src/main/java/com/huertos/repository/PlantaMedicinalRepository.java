package com.huertos.repository;

import com.huertos.model.PlantaMedicinal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantaMedicinalRepository extends JpaRepository<PlantaMedicinal, Long> {

    boolean existsByNombreCientificoIgnoreCase(String nombreCientifico);
}
