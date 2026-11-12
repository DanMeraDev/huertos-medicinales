package com.huertos.repository;

import com.huertos.model.TalentoComunidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalentoComunidadRepository extends JpaRepository<TalentoComunidad, Long> {

    List<TalentoComunidad> findByComunidadIdAndActivoTrue(Long comunidadId);

    boolean existsByComunidadIdAndNombreInsigniaAndActivoTrue(Long comunidadId, String nombreInsignia);
}
