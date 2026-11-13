package com.huertos.repository;

import com.huertos.model.RegistroActividadMiembro;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroActividadMiembroRepository extends JpaRepository<RegistroActividadMiembro, Long> {

    long countByMiembroIdAndActividadComunidadId(Long miembroId, Long actividadComunidadId);

    List<RegistroActividadMiembro> findByMiembroIdOrderByFechaActividadDesc(Long miembroId);

    boolean existsByMiembroIdAndActividadComunidadIdAndFechaActividad(Long miembroId, Long actividadComunidadId, LocalDate fechaActividad);

    @Query("select coalesce(sum(r.horasTrabajadas), 0) from RegistroActividadMiembro r "
            + "where r.miembro.id = :miembroId and r.fechaActividad = :fecha")
    long sumarHorasDelDia(@Param("miembroId") Long miembroId, @Param("fecha") LocalDate fecha);
}
