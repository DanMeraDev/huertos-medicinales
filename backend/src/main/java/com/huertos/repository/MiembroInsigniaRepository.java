package com.huertos.repository;

import com.huertos.model.MiembroInsignia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiembroInsigniaRepository extends JpaRepository<MiembroInsignia, Long> {

    boolean existsByMiembroIdAndSaberAncestralId(Long miembroId, Long saberAncestralId);
}
