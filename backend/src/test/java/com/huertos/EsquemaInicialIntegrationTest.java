package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprueba contra una MariaDB real que las migraciones dejan el esquema que usan los casos de uso.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class EsquemaInicialIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void creaLasTablasDeUsuariosComunidadesYMiembros() {
        List<String> tablas = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()",
                String.class);

        assertThat(tablas).contains(
                "usuarios", "solicitudes_comunidad", "comunidades", "miembros", "comunidad_miembros");
    }

    @Test
    void registraLaSolicitudComoPendiente() {
        jdbcTemplate.update("""
                INSERT INTO solicitudes_comunidad
                    (nombre_comunidad, tipo_organizacion, provincia, parroquia, direccion, latitud, longitud,
                     diseno_principal, subtipos_solicitados, cedula_solicitante, nombre_solicitante,
                     correo_solicitante, celular_solicitante)
                VALUES ('Hampi Chakra', 'BARRIO', 'Pichincha', 'Calderón', 'Calle 1', -0.0950000, -78.4220000,
                        'EN_TIERRA', 'SURCOS_WACHU', '1712345678', 'Rosa Quishpe',
                        'rosa@correo.com', '0991234567')
                """);

        String estado = jdbcTemplate.queryForObject(
                "SELECT estado FROM solicitudes_comunidad WHERE nombre_comunidad = 'Hampi Chakra'", String.class);
        assertThat(estado).isEqualTo("PENDIENTE");
    }

    @Test
    void impideQueUnResponsableTengaDosComunidades() {
        Long responsableId = insertarResponsable();
        insertarComunidad("Hampi Chakra", responsableId);

        assertThatThrownBy(() -> insertarComunidad("Sumak Yura", responsableId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void permiteQueUnMiembroParticipeEnVariasComunidadesUnaVezEnCada() {
        Long primera = insertarComunidad("Hampi Chakra", insertarResponsable());
        jdbcTemplate.update("""
                INSERT INTO usuarios (nombre_completo, identificacion, genero, email, contrasena, rol)
                VALUES ('Luis Tipán', '1798765432', 'MASCULINO', 'luis@correo.com', 'hash', 'RESPONSABLE_COMUNIDAD')
                """);
        Long segunda = insertarComunidad("Sumak Yura",
                jdbcTemplate.queryForObject("SELECT id FROM usuarios WHERE email = 'luis@correo.com'", Long.class));
        jdbcTemplate.update("""
                INSERT INTO miembros (cedula, nombres, apellidos, fecha_nacimiento, genero)
                VALUES ('1711111111', 'Ana', 'Guamán', '2000-01-15', 'FEMENINO')
                """);
        Long miembroId = jdbcTemplate.queryForObject(
                "SELECT id FROM miembros WHERE cedula = '1711111111'", Long.class);

        vincular(primera, miembroId);
        vincular(segunda, miembroId);

        assertThatThrownBy(() -> vincular(primera, miembroId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long insertarResponsable() {
        jdbcTemplate.update("""
                INSERT INTO usuarios (nombre_completo, identificacion, genero, email, contrasena, rol)
                VALUES ('Rosa Quishpe', '1712345678', 'FEMENINO', 'rosa@correo.com', 'hash', 'RESPONSABLE_COMUNIDAD')
                """);
        return jdbcTemplate.queryForObject("SELECT id FROM usuarios WHERE email = 'rosa@correo.com'", Long.class);
    }

    private Long insertarComunidad(String nombre, Long responsableId) {
        jdbcTemplate.update("""
                INSERT INTO comunidades
                    (nombre, tipo_organizacion, provincia, parroquia, direccion,
                     telefono_contacto, correo_contacto, responsable_id)
                VALUES (?, 'BARRIO', 'Pichincha', 'Calderón', 'Calle 1', '0991234567', 'contacto@correo.com', ?)
                """, nombre, responsableId);
        return jdbcTemplate.queryForObject("SELECT id FROM comunidades WHERE nombre = ?", Long.class, nombre);
    }

    private void vincular(Long comunidadId, Long miembroId) {
        jdbcTemplate.update("""
                INSERT INTO comunidad_miembros (comunidad_id, miembro_id, fecha_ingreso)
                VALUES (?, ?, '2026-11-09')
                """, comunidadId, miembroId);
    }
}
