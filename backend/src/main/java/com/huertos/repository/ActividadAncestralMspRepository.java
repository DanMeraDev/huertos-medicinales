package com.huertos.repository;

import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.EstadoVerificacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActividadAncestralMspRepository extends JpaRepository<ActividadAncestralMsp, Long> {

    List<ActividadAncestralMsp> findByEstadoVerificacionOrderByCreadoEnAsc(EstadoVerificacion estado);
}
