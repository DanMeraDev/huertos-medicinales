package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprueba contra una MariaDB real que la migración V8 carga el catálogo inicial de plantas medicinales
 * tomado de los Lineamientos del MSP (2017) para la implementación de huertos medicinales.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class PlantasMspSeedIntegrationTest {

    private static final String SOLO_SEMILLA = "fotografia_url LIKE 'plantas/%'";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void cargaVeintePlantasActivasDelMsp() {
        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM plantas WHERE activa = TRUE AND " + SOLO_SEMILLA, Integer.class);

        assertThat(total).isEqualTo(20);
    }

    @Test
    void cadaPlantaTieneLosDatosObligatoriosDeLaFicha() {
        Integer incompletas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM plantas
                WHERE %s
                  AND (TRIM(nombre_comun) = '' OR TRIM(nombre_cientifico) = '' OR TRIM(familia_botanica) = ''
                       OR TRIM(usos_medicinales) = '' OR ciclo_cultivo IS NULL OR origen IS NULL
                       OR altitud_min_msnm IS NULL OR altitud_max_msnm IS NULL
                       OR regiones_distribucion IS NULL OR preparacion_dosificacion IS NULL
                       OR contraindicaciones IS NULL)
                """.formatted(SOLO_SEMILLA), Integer.class);

        assertThat(incompletas).isZero();
    }

    @Test
    void usaSoloValoresValidosDeLosEnums() {
        Integer invalidas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM plantas
                WHERE %s
                  AND (cualidad_termica NOT IN ('CALIENTE', 'FRIA')
                       OR ciclo_cultivo NOT IN ('CORTO', 'LARGO')
                       OR origen NOT IN ('NATIVA', 'ENDEMICA', 'INTRODUCIDA'))
                """.formatted(SOLO_SEMILLA), Integer.class);

        assertThat(invalidas).isZero();
    }

    @Test
    void separaPlantasCalientesYFriasSegunElAnexo5() {
        List<Map<String, Object>> conteo = jdbcTemplate.queryForList("""
                SELECT cualidad_termica, COUNT(*) AS total FROM plantas
                WHERE %s GROUP BY cualidad_termica ORDER BY cualidad_termica
                """.formatted(SOLO_SEMILLA));

        assertThat(conteo)
                .extracting(fila -> fila.get("cualidad_termica"), fila -> ((Number) fila.get("total")).intValue())
                .containsExactly(
                        tuple("CALIENTE", 14),
                        tuple("FRIA", 6));
    }

    @Test
    void cadaPlantaTieneAlMenosUnaCategoriaValida() {
        Integer sinCategoria = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM plantas p
                WHERE %s
                  AND NOT EXISTS (SELECT 1 FROM planta_categorias c WHERE c.planta_id = p.id)
                """.formatted(SOLO_SEMILLA), Integer.class);
        Integer categoriasInvalidas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM planta_categorias
                WHERE categoria NOT IN ('ANTIESPASMODICA', 'ANTIINFLAMATORIA', 'ANTIOXIDANTE', 'ANTIPARASITARIA',
                                        'ANTIPIRETICA', 'ASTRINGENTE', 'CICATRIZANTE', 'DIURETICA', 'ENERGETICA',
                                        'DEPURATIVA', 'DESCONGESTIONANTE', 'ESTIMULANTE', 'EXPECTORANTE', 'SEDANTE')
                """, Integer.class);

        assertThat(sinCategoria).isZero();
        assertThat(categoriasInvalidas).isZero();
    }

    @Test
    void registraLaManzanillaConSuFichaCompleta() {
        Map<String, Object> manzanilla = jdbcTemplate.queryForMap("""
                SELECT nombre_comun, familia_botanica, cualidad_termica, ciclo_cultivo, origen, fotografia_url
                FROM plantas WHERE nombre_cientifico = 'Matricaria chamomilla'
                """);
        List<String> categorias = jdbcTemplate.queryForList("""
                SELECT c.categoria FROM planta_categorias c JOIN plantas p ON p.id = c.planta_id
                WHERE p.nombre_cientifico = 'Matricaria chamomilla'
                """, String.class);

        assertThat(manzanilla)
                .containsEntry("nombre_comun", "Manzanilla")
                .containsEntry("familia_botanica", "Asteraceae")
                .containsEntry("cualidad_termica", "CALIENTE")
                .containsEntry("ciclo_cultivo", "CORTO")
                .containsEntry("origen", "INTRODUCIDA")
                .containsEntry("fotografia_url", "plantas/manzanilla.jpg");
        assertThat(categorias).containsExactlyInAnyOrder("ANTIESPASMODICA", "SEDANTE", "ANTIINFLAMATORIA");
    }

    @Test
    void guardaLosNombresKichwaDelDocumentoDelMsp() {
        List<String> conKichwa = jdbcTemplate.queryForList("""
                SELECT nombre_kichwa FROM plantas WHERE %s AND nombre_kichwa IS NOT NULL
                """.formatted(SOLO_SEMILLA), String.class);

        assertThat(conKichwa).contains("Tsini", "Paiku", "Chillka", "Chakra kiwa", "Tani", "Caballo chupa");
    }

    @Test
    void guardaLaFotoComoRutaRelativaDentroDeLaCarpetaDePlantas() {
        Integer fueraDeFormato = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM plantas
                WHERE %s AND fotografia_url NOT REGEXP '^plantas/[a-z-]+\\\\.jpg$'
                """.formatted(SOLO_SEMILLA), Integer.class);
        Integer distintas = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT fotografia_url) FROM plantas WHERE " + SOLO_SEMILLA, Integer.class);

        assertThat(fueraDeFormato).isZero();
        assertThat(distintas).isEqualTo(20);
    }

    @Test
    void lasAltitudesRespetanElRango() {
        Integer fueraDeRango = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM plantas
                WHERE %s AND (altitud_min_msnm < 0 OR altitud_max_msnm > 4500 OR altitud_min_msnm > altitud_max_msnm)
                """.formatted(SOLO_SEMILLA), Integer.class);

        assertThat(fueraDeRango).isZero();
    }

    @Test
    void losTextosNoCitanAnexosNiTablasDelDocumentoDelMsp() {
        Integer conReferencias = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM plantas
                WHERE %s
                  AND (usos_medicinales LIKE '%%Anexo%%' OR usos_medicinales LIKE '%%Tabla%%'
                       OR preparacion_dosificacion LIKE '%%Anexo%%' OR preparacion_dosificacion LIKE '%%Tabla%%'
                       OR contraindicaciones LIKE '%%Anexo%%' OR contraindicaciones LIKE '%%Tabla%%')
                """.formatted(SOLO_SEMILLA), Integer.class);

        assertThat(conReferencias).isZero();
    }

    @Test
    void conservaLosTextosSinLaReferenciaAlDocumento() {
        String valeriana = jdbcTemplate.queryForObject(
                "SELECT usos_medicinales FROM plantas WHERE nombre_cientifico = 'Valeriana officinalis'", String.class);
        String marco = jdbcTemplate.queryForObject(
                "SELECT usos_medicinales FROM plantas WHERE nombre_cientifico = 'Ambrosia arborescens'", String.class);

        assertThat(valeriana).isEqualTo("Sedante y antiespasmódica: ayuda en el insomnio, la ansiedad y el "
                + "nerviosismo, y calma los calambres.");
        assertThat(marco).isEqualTo("Planta protectora del huerto. Se usa en limpias energéticas y en baños y "
                + "frotaciones para dolores del cuerpo y reumatismo. Ingrediente del abono líquido natural.");
    }

    @Test
    void cadaFotoDeLaSemillaTieneSuArchivoEnElJar() {
        List<String> rutas = jdbcTemplate.queryForList(
                "SELECT fotografia_url FROM plantas WHERE " + SOLO_SEMILLA, String.class);

        assertThat(rutas)
                .filteredOn(ruta -> !new ClassPathResource("fotos-iniciales/" + ruta).exists())
                .isEmpty();
    }

    @Test
    void cadaFotoInicialTieneSuFilaDeCreditos() throws IOException {
        List<String> fotos = Arrays.stream(new PathMatchingResourcePatternResolver()
                        .getResources("classpath:fotos-iniciales/plantas/*"))
                .map(Resource::getFilename)
                .toList();
        List<String> creditos = new ClassPathResource("fotos-iniciales/creditos.csv")
                .getContentAsString(StandardCharsets.UTF_8)
                .lines()
                .skip(1)
                .map(linea -> linea.substring(0, linea.indexOf(',')))
                .toList();

        assertThat(creditos).containsExactlyInAnyOrderElementsOf(fotos);
    }
}
