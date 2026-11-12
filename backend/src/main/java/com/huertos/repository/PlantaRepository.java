package com.huertos.repository;

import com.huertos.model.Planta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlantaRepository extends JpaRepository<Planta, Long>, JpaSpecificationExecutor<Planta> {

    Optional<Planta> findByIdAndActivaTrue(Long id);

    Page<Planta> findAllByActivaTrue(Pageable pageable);

    boolean existsByNombreCientificoIgnoreCase(String nombreCientifico);

    @Query("""
            SELECT p FROM Planta p
            WHERE p.activa = true
              AND (p.nombreComun LIKE CONCAT('%', :#{escape(#criterio)}, '%') ESCAPE :#{escapeCharacter()}
                OR p.nombreCientifico LIKE CONCAT('%', :#{escape(#criterio)}, '%') ESCAPE :#{escapeCharacter()}
                OR p.nombreKichwa LIKE CONCAT('%', :#{escape(#criterio)}, '%') ESCAPE :#{escapeCharacter()})
            ORDER BY p.nombreComun, p.id
            """)
    List<Planta> buscarActivasPorNombre(@Param("criterio") String criterio);
}
