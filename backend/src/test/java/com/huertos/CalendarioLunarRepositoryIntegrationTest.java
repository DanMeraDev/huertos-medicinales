package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.model.CalendarioLunar;
import com.huertos.repository.CalendarioLunarRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CalendarioLunarRepositoryIntegrationTest {

    @Autowired
    private CalendarioLunarRepository calendarioLunarRepository;

    @Test
    void encuentraElPeriodoQueContieneLaFecha() {
        Optional<CalendarioLunar> periodo = calendarioLunarRepository
                .findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 11));

        assertThat(periodo).isPresent();
        assertThat(periodo.get().getNombreKichwa()).isEqualTo("Yana Killa");
        assertThat(periodo.get().getFechaInicio()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(periodo.get().getFechaFin()).isEqualTo(LocalDate.of(2026, 11, 16));
    }

    @Test
    void noEncuentraPeriodoParaUnaFechaSinCatalogo() {
        Optional<CalendarioLunar> periodo = calendarioLunarRepository
                .findByFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1));

        assertThat(periodo).isEmpty();
    }
}
