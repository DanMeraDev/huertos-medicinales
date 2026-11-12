package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprueba contra una MariaDB real que la migración V9 carga el calendario lunar con períodos
 * continuos que cubren dos años y que cada fecha consultada pertenece a un solo período.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class CalendarioLunarSeedIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cargaLosPeriodosLunaresDeDosAnios() {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario_lunar", Integer.class);

        assertThat(total).isEqualTo(100);
    }

    @Test
    void losPeriodosCubrenElRangoCompletoYNoSeTraslapan() {
        List<Map<String, Object>> periodos = jdbcTemplate.queryForList(
                "SELECT fecha_inicio, fecha_fin FROM calendario_lunar ORDER BY fecha_inicio");

        assertThat(periodos).hasSize(100);
        assertThat(periodos.get(0).get("fecha_inicio")).isEqualTo(java.sql.Date.valueOf("2026-01-01"));
        assertThat(periodos.get(periodos.size() - 1).get("fecha_fin"))
                .isEqualTo(java.sql.Date.valueOf("2027-12-31"));

        for (int i = 1; i < periodos.size(); i++) {
            LocalDate finAnterior = ((java.sql.Date) periodos.get(i - 1).get("fecha_fin")).toLocalDate();
            LocalDate inicioActual = ((java.sql.Date) periodos.get(i).get("fecha_inicio")).toLocalDate();
            assertThat(inicioActual).isEqualTo(finAnterior.plusDays(1));
        }
    }

    @Test
    void cadaFechaPerteneceASoloUnPeriodo() {
        Integer coincidencias = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM calendario_lunar
                WHERE '2026-11-11' BETWEEN fecha_inicio AND fecha_fin
                """, Integer.class);

        assertThat(coincidencias).isEqualTo(1);
    }

    @Test
    void usaSoloValoresValidosDeLosEnums() {
        Integer invalidas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM calendario_lunar
                WHERE fase_lunar NOT IN ('YANA_KILLA_OSCURA', 'WINAK_KILLA_CRECIENTE',
                                        'PURA_KILLA_LLENA', 'WANUK_KILLA_MENGUANTE')
                """, Integer.class);

        assertThat(invalidas).isZero();
    }

    @Test
    void cadaPeriodoTieneNombresPracticasYConsejoDeKinti() {
        Integer incompletos = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM calendario_lunar
                WHERE TRIM(nombre_kichwa) = '' OR TRIM(nombre_espanol) = ''
                   OR TRIM(practicas_recomendadas) = '' OR TRIM(consejo_kinti) = ''
                """, Integer.class);

        assertThat(incompletos).isZero();
    }

    @Test
    void conservaLosNombresKichwaDeLasFases() {
        List<String> nombres = jdbcTemplate.queryForList(
                "SELECT DISTINCT nombre_kichwa FROM calendario_lunar ORDER BY nombre_kichwa", String.class);

        assertThat(nombres).containsExactlyInAnyOrder(
                "Pura Killa", "Wañuk Killa", "Wiñak Killa", "Yana Killa");
    }
}
