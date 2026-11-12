package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.huertos.dto.ComunidadResponse;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.ComunidadService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ComunidadIntegrationTest {

    private static final String NOMBRE = "Allpa Viva Integración";

    @Autowired
    private ComunidadService comunidadService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Usuario responsable;

    @BeforeEach
    void guardarResponsable() {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Segundo Amaguaña");
        usuario.setIdentificacion("1710034065");
        usuario.setFechaNacimiento(LocalDate.of(1990, 5, 14));
        usuario.setGenero(Genero.MASCULINO);
        usuario.setCorreo("segundo.integracion@correo.com");
        usuario.setPasswordHash("hash");
        usuario.setRequiereCambioClave(true);
        usuario.setRol(Rol.RESPONSABLE_COMUNIDAD);
        usuario.setActivo(true);
        usuario.setCreadoEn(LocalDateTime.of(2026, 11, 11, 10, 0));
        responsable = usuarioRepository.save(usuario);
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM huertos WHERE nombre_huerto = ?", NOMBRE);
        jdbcTemplate.update("DELETE FROM comunidades WHERE nombre = ?", NOMBRE);
        usuarioRepository.delete(responsable);
    }

    @Test
    void guardaLaComunidadYSuHuertoBaseSinSubhuertos() {
        ComunidadResponse respuesta = comunidadService.registrarComunidadDesdeSolicitud(
                solicitud(DisenoPrincipal.EN_TIERRA), responsable);

        Map<String, Object> comunidad = jdbcTemplate.queryForMap(
                "SELECT responsable_id, puntos_comunidad, activa FROM comunidades WHERE id = ?", respuesta.id());
        Map<String, Object> huerto = jdbcTemplate.queryForMap(
                "SELECT id, nombre_huerto, area_total_m2, tipo_huerto FROM huertos WHERE comunidad_id = ?",
                respuesta.id());
        Integer subhuertos = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subhuertos WHERE huerto_id = ?", Integer.class, huerto.get("id"));

        assertThat(((Number) comunidad.get("responsable_id")).longValue()).isEqualTo(responsable.getId());
        assertThat(((Number) comunidad.get("puntos_comunidad")).intValue()).isZero();
        assertThat(comunidad.get("activa")).isEqualTo(true);
        assertThat(huerto.get("nombre_huerto")).isEqualTo(NOMBRE);
        assertThat(huerto.get("area_total_m2")).isNull();
        assertThat(huerto.get("tipo_huerto")).isEqualTo("BASICO");
        assertThat(subhuertos).isZero();
    }

    @Test
    void revierteLaComunidadSiFallaElHuerto() {
        assertThatThrownBy(() -> comunidadService.registrarComunidadDesdeSolicitud(solicitud(null), responsable))
                .isInstanceOf(DataAccessException.class);

        Integer comunidades = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comunidades WHERE nombre = ?", Integer.class, NOMBRE);
        assertThat(comunidades).isZero();
    }

    private SolicitudComunidad solicitud(DisenoPrincipal disenoPrincipal) {
        SolicitudComunidad solicitud = new SolicitudComunidad();
        solicitud.setNombreComunidad(NOMBRE);
        solicitud.setTipoOrganizacion(TipoOrganizacion.BARRIO);
        solicitud.setProvincia("Pichincha");
        solicitud.setParroquia("Calderón");
        solicitud.setDireccion("Calle de las Rosas");
        solicitud.setLatitud(new BigDecimal("-0.0987123"));
        solicitud.setLongitud(new BigDecimal("-78.4321456"));
        solicitud.setAltitudMsnm(2650);
        solicitud.setDisenoPrincipal(disenoPrincipal);
        solicitud.setCorreoSolicitante("allpa@correo.com");
        solicitud.setCelularSolicitante("0991234567");
        return solicitud;
    }
}
