package com.huertos.repository;

import com.huertos.model.Miembro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiembroRepository extends JpaRepository<Miembro, Long> {

    Optional<Miembro> findByCedula(String cedula);

    List<Miembro> findByActivoTrueOrderByPuntosTotalesDescCreadoEnAsc();

    Optional<Miembro> findByCedulaAndActivoTrue(String cedula);
}
