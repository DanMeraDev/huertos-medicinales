package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprueba contra una MariaDB real que las migraciones V3 y V4 dejan el esquema del catálogo de plantas,
 * el huerto, la bodega y las siembras.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EsquemaPlantasBodegaIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void creaLasTablasDePlantasHuertosBodegaYSiembras() {
        List<String> tablas = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class);

        assertThat(tablas).contains(
                "plantas", "planta_categorias", "huertos", "subhuertos",
                "bodega_material_vegetal", "bajas_material_vegetal", "siembras");
    }

    @Test
    void registraUnaPlantaActivaConVariasCategoriasSinRepetirlas() {
        Long plantaId = insertarPlanta("Planta de prueba", "Probatoria ficticia");
        jdbcTemplate.update("INSERT INTO planta_categorias (planta_id, categoria) VALUES (?, 'DEPURATIVA')", plantaId);
        jdbcTemplate.update("INSERT INTO planta_categorias (planta_id, categoria) VALUES (?, 'DIURETICA')", plantaId);

        Boolean activa = jdbcTemplate.queryForObject(
                "SELECT activa FROM plantas WHERE id = ?", Boolean.class, plantaId);
        assertThat(activa).isTrue();
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO planta_categorias (planta_id, categoria) VALUES (?, 'DEPURATIVA')", plantaId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void impideDosPlantasConElMismoNombreCientifico() {
        insertarPlanta("Planta de prueba", "Probatoria ficticia");

        assertThatThrownBy(() -> insertarPlanta("Otra planta de prueba", "Probatoria ficticia"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void impideUnRangoDeAltitudInvertido() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO plantas (nombre_comun, nombre_cientifico, familia_botanica, cualidad_termica,
                                     usos_medicinales, altitud_min_msnm, altitud_max_msnm)
                VALUES ('Llantén', 'Plantago major', 'Plantaginaceae', 'FRIA', 'Inflamación', 3000, 1000)
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registraQuienActualizoYQuienDioDeBajaUnaPlantaConSuMotivo() {
        Long plantaId = insertarPlanta("Planta de prueba", "Probatoria ficticia");
        Long tecnicoId = insertarTecnicoMsp();

        jdbcTemplate.update("UPDATE plantas SET usos_medicinales = 'Digestiva', actualizado_por_id = ? WHERE id = ?",
                tecnicoId, plantaId);
        jdbcTemplate.update("""
                UPDATE plantas
                SET activa = FALSE, motivo_baja = 'Registro duplicado', fecha_baja = NOW(), dado_de_baja_por_id = ?
                WHERE id = ?
                """, tecnicoId, plantaId);

        Integer registradas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM plantas
                WHERE id = ? AND activa = FALSE AND motivo_baja = 'Registro duplicado' AND fecha_baja IS NOT NULL
                  AND dado_de_baja_por_id = ? AND actualizado_por_id = ?
                """, Integer.class, plantaId, tecnicoId, tecnicoId);
        assertThat(registradas).isEqualTo(1);
    }

    @Test
    void impideDarDeBajaUnaPlantaConUnUsuarioInexistente() {
        Long plantaId = insertarPlanta("Planta de prueba", "Probatoria ficticia");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE plantas SET activa = FALSE, dado_de_baja_por_id = 999999 WHERE id = ?", plantaId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void guardaCantidadesConDecimalesYNoPermiteStockNegativo() {
        Long loteId = insertarLote(insertarComunidad(), insertarPlanta("Planta de prueba", "Probatoria ficticia"));

        BigDecimal cantidad = jdbcTemplate.queryForObject(
                "SELECT cantidad_disponible FROM bodega_material_vegetal WHERE id = ?", BigDecimal.class, loteId);
        assertThat(cantidad).isEqualByComparingTo("12.50");
        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE bodega_material_vegetal SET cantidad_disponible = -1 WHERE id = ?", loteId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void permiteUnSoloHuertoPorComunidad() {
        Long comunidadId = insertarComunidad();
        insertarHuerto(comunidadId);

        assertThatThrownBy(() -> insertarHuerto(comunidadId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void creaElHuertoAlAprobarSinAreaYLaCompletaElResponsableDespues() {
        Long comunidadId = insertarComunidad();
        jdbcTemplate.update("""
                INSERT INTO huertos (comunidad_id, nombre_huerto, latitud, longitud, altitud_msnm, diseno_principal)
                VALUES (?, 'Hampi Chakra', -0.0950000, -78.4220000, 2800, 'EN_TIERRA')
                """, comunidadId);

        BigDecimal areaAlAprobar = jdbcTemplate.queryForObject(
                "SELECT area_total_m2 FROM huertos WHERE comunidad_id = ?", BigDecimal.class, comunidadId);
        assertThat(areaAlAprobar).isNull();

        jdbcTemplate.update("UPDATE huertos SET area_total_m2 = 120.50 WHERE comunidad_id = ?", comunidadId);
        BigDecimal areaCompletada = jdbcTemplate.queryForObject(
                "SELECT area_total_m2 FROM huertos WHERE comunidad_id = ?", BigDecimal.class, comunidadId);
        assertThat(areaCompletada).isEqualByComparingTo("120.50");
    }

    @Test
    void impideUnAreaDeHuertoNoPositiva() {
        Long comunidadId = insertarComunidad();
        Long huertoId = insertarHuerto(comunidadId);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "UPDATE huertos SET area_total_m2 = 0 WHERE id = ?", huertoId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void registraUnaSiembraDesdeUnLoteYSuPerdidaConCausa() {
        Long comunidadId = insertarComunidad();
        Long loteId = insertarLote(comunidadId, insertarPlanta("Planta de prueba", "Probatoria ficticia"));
        Long huertoId = insertarHuerto(comunidadId);
        jdbcTemplate.update("""
                INSERT INTO subhuertos (huerto_id, nombre_zona, subtipo, area_m2)
                VALUES (?, 'Wachu Norte', 'SURCOS_WACHU', 10.00)
                """, huertoId);
        Long subhuertoId = jdbcTemplate.queryForObject(
                "SELECT id FROM subhuertos WHERE huerto_id = ?", Long.class, huertoId);

        jdbcTemplate.update("""
                INSERT INTO siembras (subhuerto_id, bodega_material_id, cantidad_sembrada, fecha_siembra,
                                      fase_lunar_siembra)
                VALUES (?, ?, 2.50, '2026-11-10', 'WINAK_KILLA_CRECIENTE')
                """, subhuertoId, loteId);
        jdbcTemplate.update("""
                UPDATE siembras SET estado_cultivo = 'PERDIDO', causa_perdida = 'HELADA_CLIMA_EXTREMO'
                WHERE subhuerto_id = ?
                """, subhuertoId);

        String causa = jdbcTemplate.queryForObject(
                "SELECT causa_perdida FROM siembras WHERE subhuerto_id = ?", String.class, subhuertoId);
        assertThat(causa).isEqualTo("HELADA_CLIMA_EXTREMO");
    }

    private Long insertarPlanta(String nombreComun, String nombreCientifico) {
        jdbcTemplate.update("""
                INSERT INTO plantas (nombre_comun, nombre_cientifico, familia_botanica, cualidad_termica,
                                     usos_medicinales)
                VALUES (?, ?, 'Asteraceae', 'CALIENTE', 'Estimula la producción de bilis')
                """, nombreComun, nombreCientifico);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM plantas WHERE nombre_comun = ?", Long.class, nombreComun);
    }

    private Long insertarTecnicoMsp() {
        jdbcTemplate.update("""
                INSERT INTO usuarios (nombre_completo, identificacion, fecha_nacimiento, genero, correo, password_hash, rol)
                VALUES ('Carlos Andrade', '1700000001', '1990-05-14', 'MASCULINO', 'carlos@msp.gob.ec', 'hash', 'TECNICO_MSP')
                """);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM usuarios WHERE correo = 'carlos@msp.gob.ec'", Long.class);
    }

    private Long insertarComunidad() {
        jdbcTemplate.update("""
                INSERT INTO usuarios (nombre_completo, identificacion, fecha_nacimiento, genero, correo, password_hash, rol)
                VALUES ('Rosa Quishpe', '1712345678', '1990-05-14', 'FEMENINO', 'rosa@correo.com', 'hash', 'RESPONSABLE_COMUNIDAD')
                """);
        Long responsableId = jdbcTemplate.queryForObject(
                "SELECT id FROM usuarios WHERE correo = 'rosa@correo.com'", Long.class);
        jdbcTemplate.update("""
                INSERT INTO comunidades
                    (nombre, tipo_organizacion, provincia, parroquia, direccion,
                     telefono_contacto, correo_contacto, responsable_id)
                VALUES ('Hampi Chakra', 'BARRIO', 'Pichincha', 'Calderón', 'Calle 1',
                        '0991234567', 'contacto@correo.com', ?)
                """, responsableId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM comunidades WHERE nombre = 'Hampi Chakra'", Long.class);
    }

    private Long insertarLote(Long comunidadId, Long plantaId) {
        jdbcTemplate.update("""
                INSERT INTO bodega_material_vegetal
                    (comunidad_id, planta_id, tipo_material, cantidad_disponible, unidad_medida, fecha_ingreso)
                VALUES (?, ?, 'SEMILLAS', 12.50, 'GRAMOS', '2026-11-10')
                """, comunidadId, plantaId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM bodega_material_vegetal WHERE comunidad_id = ?", Long.class, comunidadId);
    }

    private Long insertarHuerto(Long comunidadId) {
        jdbcTemplate.update("""
                INSERT INTO huertos (comunidad_id, nombre_huerto, latitud, longitud, area_total_m2, diseno_principal)
                VALUES (?, 'Huerto San Juan', -0.0950000, -78.4220000, 120.50, 'EN_TIERRA')
                """, comunidadId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM huertos WHERE comunidad_id = ?", Long.class, comunidadId);
    }
}
