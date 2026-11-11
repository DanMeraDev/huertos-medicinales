package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EsquemaActividadesInsigniasIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long comunidadId;
    private Long huertoId;
    private Long miembroId;
    private Long saberId;

    @BeforeEach
    void prepararComunidadConHuertoMiembroYSaber() {
        jdbcTemplate.update("""
                INSERT INTO usuarios (nombre_completo, identificacion, fecha_nacimiento, genero, correo, password_hash, rol)
                VALUES ('Rosa Quishpe', '1712345678', '1990-05-14', 'FEMENINO', 'rosa@correo.com', 'hash', 'RESPONSABLE_COMUNIDAD')
                """);
        Long responsableId = id("SELECT id FROM usuarios WHERE correo = 'rosa@correo.com'");
        jdbcTemplate.update("""
                INSERT INTO comunidades
                    (nombre, tipo_organizacion, provincia, parroquia, direccion,
                     telefono_contacto, correo_contacto, responsable_id)
                VALUES ('Hampi Chakra', 'BARRIO', 'Pichincha', 'Calderón', 'Calle 1', '0991234567', 'contacto@correo.com', ?)
                """, responsableId);
        comunidadId = id("SELECT id FROM comunidades WHERE nombre = 'Hampi Chakra'");
        jdbcTemplate.update("""
                INSERT INTO huertos (comunidad_id, nombre_huerto, latitud, longitud, diseno_principal)
                VALUES (?, 'Hampi Chakra', -0.0950000, -78.4220000, 'EN_TIERRA')
                """, comunidadId);
        huertoId = id("SELECT id FROM huertos WHERE comunidad_id = " + comunidadId);
        jdbcTemplate.update("""
                INSERT INTO miembros
                    (cedula, nombres, apellidos, fecha_nacimiento, genero, consentimiento_datos, consentimiento_en)
                VALUES ('1711111111', 'Ana', 'Guamán', '2000-01-15', 'FEMENINO', TRUE, '2026-11-09 10:00:00')
                """);
        miembroId = id("SELECT id FROM miembros WHERE cedula = '1711111111'");
        jdbcTemplate.update("""
                INSERT INTO saberes_ancestrales
                    (orden_paso, codigo_hito, titulo, categoria, fase_lunar_ideal, horario_energetico,
                     contenido_educativo, nombre_insignia_oficial)
                VALUES (1, 'PREP-TIERRA', 'Preparación de la tierra', 'PREPARACION_TIERRA', 'WINAK_KILLA_CRECIENTE',
                        '05:00 a 07:00', 'Se afloja la tierra con herramientas tradicionales.', 'Guardián de la Allpa')
                """);
        saberId = id("SELECT id FROM saberes_ancestrales WHERE orden_paso = 1");
    }

    @Test
    void creaLasTablasDeActividadesSaberesEInsignias() {
        List<String> tablas = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class);

        assertThat(tablas).contains(
                "catalogo_actividades_comunidad", "talentos_comunidad", "registro_actividades_miembro",
                "calendario_lunar", "saberes_ancestrales", "actividades_ancestrales_msp",
                "actividad_msp_participantes", "miembro_insignias");
    }

    @Test
    void registraLaActividadAncestralPendienteEnSuPrimerIntento() {
        Long actividadId = insertarActividadAncestral();

        Map<String, Object> actividad = jdbcTemplate.queryForMap(
                "SELECT estado_verificacion, numero_intento FROM actividades_ancestrales_msp WHERE id = ?",
                actividadId);
        assertThat(actividad.get("estado_verificacion")).isEqualTo("PENDIENTE");
        assertThat(actividad.get("numero_intento")).isEqualTo(1);
    }

    @Test
    void impideRegistrarDosVecesAlMismoParticipanteEnUnaActividad() {
        Long actividadId = insertarActividadAncestral();
        insertarParticipante(actividadId);

        assertThatThrownBy(() -> insertarParticipante(actividadId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void impideQueUnMiembroGaneDosVecesLaMismaInsigniaOficial() {
        insertarInsigniaOficial();

        assertThatThrownBy(this::insertarInsigniaOficial)
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rechazaHorasDeActividadCotidianaFueraDeUnDia() {
        jdbcTemplate.update("""
                INSERT INTO catalogo_actividades_comunidad (comunidad_id, nombre, categoria)
                VALUES (?, 'Regar con regadera', 'RIEGO')
                """, comunidadId);
        Long actividadId = id("SELECT id FROM catalogo_actividades_comunidad WHERE comunidad_id = " + comunidadId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO registro_actividades_miembro
                    (comunidad_id, miembro_id, actividad_comunidad_id, fecha_actividad, horas_trabajadas,
                     puntos_otorgados)
                VALUES (?, ?, ?, '2026-11-10', 25, 25)
                """, comunidadId, miembroId, actividadId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long insertarActividadAncestral() {
        jdbcTemplate.update("""
                INSERT INTO actividades_ancestrales_msp
                    (comunidad_id, huerto_id, saber_ancestral_id, fecha_realizacion, fase_lunar_ejecucion,
                     descripcion_bitacora, evidencia_foto_url)
                VALUES (?, ?, ?, '2026-11-08', 'WINAK_KILLA_CRECIENTE', 'Minga de preparación', 'actividades/1.jpg')
                """, comunidadId, huertoId, saberId);
        return id("SELECT MAX(id) FROM actividades_ancestrales_msp");
    }

    private void insertarParticipante(Long actividadId) {
        jdbcTemplate.update("""
                INSERT INTO actividad_msp_participantes (actividad_msp_id, miembro_id, horas_trabajadas)
                VALUES (?, ?, 3)
                """, actividadId, miembroId);
    }

    private void insertarInsigniaOficial() {
        jdbcTemplate.update("""
                INSERT INTO miembro_insignias
                    (miembro_id, comunidad_id, tipo_origen, nombre_insignia, descripcion, saber_ancestral_id,
                     fecha_obtencion)
                VALUES (?, ?, 'OFICIAL_SABER_MSP', 'Guardián de la Allpa', 'Preparó la tierra', ?, '2026-11-10')
                """, miembroId, comunidadId, saberId);
    }

    private Long id(String consulta) {
        return jdbcTemplate.queryForObject(consulta, Long.class);
    }
}
