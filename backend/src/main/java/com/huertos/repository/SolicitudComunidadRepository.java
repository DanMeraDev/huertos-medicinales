package com.huertos.repository;

import com.huertos.model.EstadoSolicitud;
import com.huertos.model.SolicitudComunidad;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SolicitudComunidadRepository extends JpaRepository<SolicitudComunidad, Long> {

    boolean existsByNombreComunidadAndEstado(String nombreComunidad, EstadoSolicitud estado);

    boolean existsByCedulaSolicitanteAndEstado(String cedula, EstadoSolicitud estado);

    List<SolicitudComunidad> findByEstadoOrderByCreadoEnAsc(EstadoSolicitud estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SolicitudComunidad> findParaResolverById(Long id);
}
