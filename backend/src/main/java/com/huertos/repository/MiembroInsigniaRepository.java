package com.huertos.repository;

import com.huertos.model.MiembroInsignia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiembroInsigniaRepository extends JpaRepository<MiembroInsignia, Long> {

    boolean existsByMiembroIdAndSaberAncestralId(Long miembroId, Long saberAncestralId);

    boolean existsByMiembroIdAndTalentoComunidadId(Long miembroId, Long talentoComunidadId);

    long countByMiembroId(Long miembroId);

    List<MiembroInsignia> findByMiembroIdOrderByFechaObtencionDesc(Long miembroId);
}
