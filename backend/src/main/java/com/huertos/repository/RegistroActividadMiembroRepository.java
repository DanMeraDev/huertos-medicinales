package com.huertos.repository;

import com.huertos.model.RegistroActividadMiembro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroActividadMiembroRepository extends JpaRepository<RegistroActividadMiembro, Long> {

    long countByMiembroIdAndActividadComunidadId(Long miembroId, Long actividadComunidadId);

    List<RegistroActividadMiembro> findByMiembroIdOrderByFechaActividadDesc(Long miembroId);
}
