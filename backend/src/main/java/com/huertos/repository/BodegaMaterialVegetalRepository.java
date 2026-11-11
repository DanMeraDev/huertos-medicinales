package com.huertos.repository;

import com.huertos.model.BodegaMaterialVegetal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodegaMaterialVegetalRepository extends JpaRepository<BodegaMaterialVegetal, Long> {

    List<BodegaMaterialVegetal> findByComunidadIdAndActivoTrue(Long comunidadId);
}

