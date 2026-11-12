package com.huertos.repository;

import com.huertos.model.Miembro;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiembroRepository extends JpaRepository<Miembro, Long> {

    Optional<Miembro> findByCedula(String cedula);

    Optional<Miembro> findByCedulaAndActivoTrue(String cedula);
}
