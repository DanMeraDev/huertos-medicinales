package com.huertos.repository;

import com.huertos.model.Subhuerto;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubhuertoRepository extends JpaRepository<Subhuerto, Long> {

    @Query("SELECT COALESCE(SUM(s.areaM2), 0) FROM Subhuerto s WHERE s.huerto.id = :huertoId AND s.activo = true")
    BigDecimal sumAreaActivaByHuertoId(@Param("huertoId") Long huertoId);
}

