package com.huertos.repository;

import com.huertos.model.Planta;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantaRepository extends JpaRepository<Planta, Long> {

    Optional<Planta> findByIdAndActivaTrue(Long id);
    Page<Planta> findAllByActivaTrue(Pageable pageable);
}
