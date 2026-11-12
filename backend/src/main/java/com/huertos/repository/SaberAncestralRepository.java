package com.huertos.repository;

import com.huertos.model.SaberAncestral;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaberAncestralRepository extends JpaRepository<SaberAncestral, Long> {

    List<SaberAncestral> findAllByOrderByOrdenPasoAsc();
}
