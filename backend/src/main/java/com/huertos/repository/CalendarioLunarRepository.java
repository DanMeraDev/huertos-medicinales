package com.huertos.repository;

import com.huertos.model.CalendarioLunar;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarioLunarRepository extends JpaRepository<CalendarioLunar, Long> {

    Optional<CalendarioLunar> findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
            LocalDate inicioNoPosterior, LocalDate finNoAnterior);
}
