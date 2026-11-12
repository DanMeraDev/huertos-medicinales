package com.huertos.repository;

import com.huertos.model.BodegaMaterialVegetal;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface BodegaMaterialVegetalRepository extends JpaRepository<BodegaMaterialVegetal, Long> {

    List<BodegaMaterialVegetal> findByComunidadIdAndActivoTrue(Long comunidadId);

    List<BodegaMaterialVegetal> findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(Long comunidadId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BodegaMaterialVegetal> findByIdAndActivoTrue(Long id);
}

