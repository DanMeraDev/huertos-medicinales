package com.huertos.repository;

import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.EstadoVerificacion;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ActividadAncestralMspRepository extends JpaRepository<ActividadAncestralMsp, Long> {

    List<ActividadAncestralMsp> findByEstadoVerificacionOrderByCreadoEnAsc(EstadoVerificacion estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ActividadAncestralMsp> findParaDictaminarById(Long id);
}
