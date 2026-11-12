package com.huertos.repository;

import com.huertos.model.EstadoSolicitud;
import com.huertos.model.SolicitudComunidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudComunidadRepository extends JpaRepository<SolicitudComunidad, Long> {

    boolean existsByNombreComunidadAndEstado(String nombreComunidad, EstadoSolicitud estado);

    boolean existsByCedulaSolicitanteAndEstado(String cedula, EstadoSolicitud estado);
}
