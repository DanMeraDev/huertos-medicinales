package com.huertos.repository;

import com.huertos.model.Planta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlantaRepository extends JpaRepository<Planta, Long> {

    Optional<Planta> findByIdAndActivaTrue(Long id);

    Page<Planta> findAllByActivaTrue(Pageable pageable);

    @Query("SELECT p FROM Planta p WHERE 1 = 0")
    List<Planta> buscarActivasPorNombre(String criterio);
}
