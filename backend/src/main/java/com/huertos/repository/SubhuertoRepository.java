package com.huertos.repository;

import com.huertos.model.Subhuerto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubhuertoRepository extends JpaRepository<Subhuerto, Long> {

    List<Subhuerto> findByHuertoIdAndActivoTrueOrderByCreadoEnAsc(Long huertoId);
}