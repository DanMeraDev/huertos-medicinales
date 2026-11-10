package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.model.Comunidad;
import com.huertos.model.ComunidadMiembro;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.EstadoSolicitud;
import com.huertos.model.Genero;
import com.huertos.model.Miembro;
import com.huertos.model.NivelMiembro;
import com.huertos.model.Rol;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ComunidadesYMiembrosMapeoIntegrationTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 9, 10, 0);

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void guardaLaSolicitudConSuRevisor() {
        Usuario tecnico = entityManager.persist(usuario("tecnico@correo.com", "1700000001", Rol.TECNICO_MSP));
        SolicitudComunidad solicitud = new SolicitudComunidad();
        solicitud.setNombreComunidad("Hampi Chakra");
        solicitud.setTipoOrganizacion(TipoOrganizacion.BARRIO);
        solicitud.setProvincia("Pichincha");
        solicitud.setParroquia("Calderón");
        solicitud.setDireccion("Calle 1");
        solicitud.setLatitud(new BigDecimal("-0.0950000"));
        solicitud.setLongitud(new BigDecimal("-78.4220000"));
        solicitud.setAltitudMsnm(2650);
        solicitud.setDisenoPrincipal(DisenoPrincipal.EN_TIERRA);
        solicitud.setSubtiposSolicitados("SURCOS_WACHU");
        solicitud.setCedulaSolicitante("1712345678");
        solicitud.setNombreSolicitante("Rosa Quishpe");
        solicitud.setCorreoSolicitante("rosa@correo.com");
        solicitud.setCelularSolicitante("0991234567");
        solicitud.setGeneroSolicitante(Genero.FEMENINO);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setMotivoRechazo("Datos de ubicación incompletos");
        solicitud.setRevisadoPor(tecnico);
        solicitud.setFechaRevision(AHORA);
        solicitud.setCreadoEn(AHORA);
        Long id = entityManager.persistAndGetId(solicitud, Long.class);
        entityManager.clear();

        SolicitudComunidad leida = entityManager.find(SolicitudComunidad.class, id);

        assertThat(leida.getEstado()).isEqualTo(EstadoSolicitud.RECHAZADA);
        assertThat(leida.getTipoOrganizacion()).isEqualTo(TipoOrganizacion.BARRIO);
        assertThat(leida.getDisenoPrincipal()).isEqualTo(DisenoPrincipal.EN_TIERRA);
        assertThat(leida.getLatitud()).isEqualByComparingTo("-0.0950000");
        assertThat(leida.getRevisadoPor().getEmail()).isEqualTo("tecnico@correo.com");
    }

    @Test
    void guardaLaComunidadConSuResponsableYSusMiembros() {
        Usuario responsable = entityManager.persist(
                usuario("rosa@correo.com", "1712345678", Rol.RESPONSABLE_COMUNIDAD));
        Comunidad comunidad = new Comunidad();
        comunidad.setNombre("Hampi Chakra");
        comunidad.setTipoOrganizacion(TipoOrganizacion.FAMILIA);
        comunidad.setProvincia("Pichincha");
        comunidad.setParroquia("Calderón");
        comunidad.setDireccion("Calle 1");
        comunidad.setTelefonoContacto("0991234567");
        comunidad.setCorreoContacto("contacto@correo.com");
        comunidad.setResponsable(responsable);
        comunidad.setActiva(true);
        comunidad.setCreadoEn(AHORA);
        entityManager.persist(comunidad);

        Miembro miembro = new Miembro();
        miembro.setCedula("1711111111");
        miembro.setNombres("Ana");
        miembro.setApellidos("Guamán");
        miembro.setFechaNacimiento(LocalDate.of(2000, 1, 15));
        miembro.setGenero(Genero.FEMENINO);
        miembro.setNivel(NivelMiembro.APRENDIZ);
        miembro.setActivo(true);
        miembro.setCreadoEn(AHORA);
        entityManager.persist(miembro);

        ComunidadMiembro membresia = new ComunidadMiembro();
        membresia.setComunidad(comunidad);
        membresia.setMiembro(miembro);
        membresia.setActivoEnComunidad(true);
        membresia.setFechaIngreso(LocalDate.of(2026, 11, 9));
        Long id = entityManager.persistAndGetId(membresia, Long.class);
        entityManager.clear();

        ComunidadMiembro leida = entityManager.find(ComunidadMiembro.class, id);

        assertThat(leida.getComunidad().getResponsable().getEmail()).isEqualTo("rosa@correo.com");
        assertThat(leida.getMiembro().getNivel()).isEqualTo(NivelMiembro.APRENDIZ);
        assertThat(leida.isActivoEnComunidad()).isTrue();
    }

    private Usuario usuario(String email, String identificacion, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Persona de prueba");
        usuario.setIdentificacion(identificacion);
        usuario.setGenero(Genero.OTRO);
        usuario.setEmail(email);
        usuario.setContrasena("hash");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setCreadoEn(AHORA);
        return usuario;
    }
}
