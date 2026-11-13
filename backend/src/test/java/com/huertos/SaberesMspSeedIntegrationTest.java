package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprueba contra una MariaDB real que la migración V11 carga la ruta secuencial de saberes ancestrales
 * tomada de los Lineamientos del MSP (2017) para la implementación de huertos medicinales.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class SaberesMspSeedIntegrationTest {

    private static final String SOLO_SEMILLA = "orden_paso BETWEEN 1 AND 11";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cargaOncePasosConsecutivosEnElOrdenDeLaRuta() {
        List<Map<String, Object>> pasos = jdbcTemplate.queryForList(
                "SELECT orden_paso, codigo_hito FROM saberes_ancestrales WHERE " + SOLO_SEMILLA
                        + " ORDER BY orden_paso");

        assertThat(pasos)
                .extracting(fila -> fila.get("orden_paso"), fila -> fila.get("codigo_hito"))
                .containsExactly(
                        tuple(1, "ABONO-NATURAL"),
                        tuple(2, "PREP-TIERRA"),
                        tuple(3, "SURCOS-CAMELLONES"),
                        tuple(4, "RITUAL-SIEMBRA"),
                        tuple(5, "SIEMBRA"),
                        tuple(6, "SENALETICA"),
                        tuple(7, "CUIDADO-RIEGO"),
                        tuple(8, "RITUAL-COSECHA"),
                        tuple(9, "COSECHA"),
                        tuple(10, "LIMPIEZA-SECADO"),
                        tuple(11, "EMPAQUETADO"));
    }

    @Test
    void cubreLasSieteCategoriasDeSaber() {
        List<String> categorias = jdbcTemplate.queryForList(
                "SELECT DISTINCT categoria FROM saberes_ancestrales WHERE " + SOLO_SEMILLA, String.class);

        assertThat(categorias).containsExactlyInAnyOrder(
                "PREPARACION_TIERRA", "ABONO_NATURAL", "RITUAL_SIEMBRA", "SIEMBRA_SENALETICA",
                "CUIDADO_RIEGO", "RITUAL_COSECHA", "SECADO_EMPAQUETADO");
    }

    @Test
    void usaLasFasesLunaresQueIndicaElMsp() {
        List<Map<String, Object>> fases = jdbcTemplate.queryForList(
                "SELECT codigo_hito, fase_lunar_ideal FROM saberes_ancestrales WHERE codigo_hito IN "
                        + "('PREP-TIERRA', 'RITUAL-SIEMBRA', 'SIEMBRA', 'COSECHA', 'EMPAQUETADO') ORDER BY orden_paso");

        assertThat(fases)
                .extracting(fila -> fila.get("codigo_hito"), fila -> fila.get("fase_lunar_ideal"))
                .containsExactly(
                        tuple("PREP-TIERRA", "YANA_KILLA_OSCURA"),
                        tuple("RITUAL-SIEMBRA", "WINAK_KILLA_CRECIENTE"),
                        tuple("SIEMBRA", "WINAK_KILLA_CRECIENTE"),
                        tuple("COSECHA", "PURA_KILLA_LLENA"),
                        tuple("EMPAQUETADO", "WANUK_KILLA_MENGUANTE"));
    }

    @Test
    void todosLosPasosTienenContenidoInsigniaYVeintePuntos() {
        Integer incompletos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM saberes_ancestrales WHERE " + SOLO_SEMILLA
                        + " AND (TRIM(contenido_educativo) = '' OR TRIM(horario_energetico) = ''"
                        + " OR TRIM(nombre_insignia_oficial) = '' OR ingredientes_herramientas IS NULL"
                        + " OR puntos_base_comunidad <> 20)",
                Integer.class);

        assertThat(incompletos).isZero();
    }

    @Test
    void lasInsigniasOficialesNoSeRepiten() {
        Integer insigniasDistintas = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT nombre_insignia_oficial) FROM saberes_ancestrales WHERE " + SOLO_SEMILLA,
                Integer.class);

        assertThat(insigniasDistintas).isEqualTo(11);
    }

    @Test
    void soloElPasoDeSurcosTraeNombreKichwaDelDocumento() {
        List<String> conKichwa = jdbcTemplate.queryForList(
                "SELECT codigo_hito FROM saberes_ancestrales WHERE " + SOLO_SEMILLA
                        + " AND titulo_kichwa IS NOT NULL", String.class);

        assertThat(conKichwa).containsExactly("SURCOS-CAMELLONES");
    }

    @Test
    void losTextosVisiblesNoCitanTablasNiAnexosDelDocumento() {
        Integer conReferencias = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM saberes_ancestrales WHERE " + SOLO_SEMILLA
                        + " AND (contenido_educativo REGEXP 'Anexo [0-9]|Tabla [0-9]'"
                        + " OR ingredientes_herramientas REGEXP 'Anexo [0-9]|Tabla [0-9]')",
                Integer.class);

        assertThat(conReferencias).isZero();
    }
}
