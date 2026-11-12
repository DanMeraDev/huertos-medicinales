package com.huertos.repository;

import com.huertos.model.ComunidadMiembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComunidadMiembroRepository extends JpaRepository<ComunidadMiembro, Long> {

    @Query("SELECT COUNT(cm) FROM ComunidadMiembro cm WHERE cm.comunidad.id = :comunidadId AND cm.activoEnComunidad = true")
    int countMiembrosActivos(@Param("comunidadId") Long comunidadId);
}
