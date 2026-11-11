package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.ActividadMspParticipante;
import com.huertos.model.CalendarioLunar;
import com.huertos.model.CategoriaActividad;
import com.huertos.model.CategoriaSaber;
import com.huertos.model.Comunidad;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.FaseLunar;
import com.huertos.model.Genero;
import com.huertos.model.Huerto;
import com.huertos.model.Miembro;
import com.huertos.model.MiembroInsignia;
import com.huertos.model.NivelMiembro;
import com.huertos.model.RegistroActividadMiembro;
import com.huertos.model.Rol;
import com.huertos.model.SaberAncestral;
import com.huertos.model.TalentoComunidad;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.TipoOrigenInsignia;
import com.huertos.model.Usuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ActividadesSaberesInsigniasMapeoIntegrationTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 10, 0);

    @Autowired
    private TestEntityManager entityManager;

    private Comunidad comunidad;
    private Miembro miembro;

    @BeforeEach
    void prepararComunidadYMiembro() {
        comunidad = entityManager.persist(comunidad(entityManager.persist(responsable())));
        miembro = entityManager.persist(miembro());
    }

    @Test
    void guardaLaActividadCotidianaConSuTalentoYSuRegistro() {
        ActividadComunidad actividad = new ActividadComunidad();
        actividad.setComunidad(comunidad);
        actividad.setNombre("Abonar camas");
        actividad.setCategoria(CategoriaActividad.ABONO_COMPOST);
        entityManager.persist(actividad);

        TalentoComunidad talento = new TalentoComunidad();
        talento.setComunidad(comunidad);
        talento.setActividadComunidad(actividad);
        talento.setNombreInsignia("Maestro del Abonaje");
        talento.setDescripcion("Abonó las camas 50 veces");
        talento.setMetaRepeticiones(50);
        Long talentoId = entityManager.persistAndGetId(talento, Long.class);

        RegistroActividadMiembro registro = new RegistroActividadMiembro();
        registro.setComunidad(comunidad);
        registro.setMiembro(miembro);
        registro.setActividadComunidad(actividad);
        registro.setFechaActividad(LocalDate.of(2026, 11, 10));
        registro.setHorasTrabajadas(2);
        registro.setPuntosOtorgados(2);
        Long registroId = entityManager.persistAndGetId(registro, Long.class);
        entityManager.flush();
        entityManager.clear();

        TalentoComunidad talentoLeido = entityManager.find(TalentoComunidad.class, talentoId);
        RegistroActividadMiembro registroLeido = entityManager.find(RegistroActividadMiembro.class, registroId);

        assertThat(talentoLeido.getActividadComunidad().getCategoria()).isEqualTo(CategoriaActividad.ABONO_COMPOST);
        assertThat(talentoLeido.getActividadComunidad().getPuntosPorHora()).isEqualTo(1);
        assertThat(talentoLeido.getIconoCodigo()).isEqualTo("insignia-semilla");
        assertThat(registroLeido.getMiembro().getCedula()).isEqualTo("1711111111");
        assertThat(registroLeido.getHorasTrabajadas()).isEqualTo(2);
        assertThat(registroLeido.getCreadoEn()).isNotNull();
    }

    @Test
    void guardaLaActividadAncestralConSusParticipantes() {
        Usuario tecnico = entityManager.persist(usuario("tecnico@msp.gob.ec", "1700000001", Rol.TECNICO_MSP));
        Huerto huerto = entityManager.persist(huerto());
        SaberAncestral saber = entityManager.persist(saber());

        ActividadAncestralMsp actividad = new ActividadAncestralMsp();
        actividad.setComunidad(comunidad);
        actividad.setHuerto(huerto);
        actividad.setSaberAncestral(saber);
        actividad.setFechaRealizacion(LocalDate.of(2026, 11, 8));
        actividad.setFaseLunarEjecucion(FaseLunar.WINAK_KILLA_CRECIENTE);
        actividad.setDescripcionBitacora("Minga de preparación de la tierra");
        actividad.setEvidenciaFotoUrl("actividades/1.jpg");
        actividad.setEstadoVerificacion(EstadoVerificacion.APROBADA);
        actividad.setVerificadoPor(tecnico);
        actividad.setFechaVerificacion(AHORA);
        ActividadMspParticipante participante = new ActividadMspParticipante();
        participante.setActividadMsp(actividad);
        participante.setMiembro(miembro);
        participante.setHorasTrabajadas(3);
        actividad.getParticipantes().add(participante);
        Long id = entityManager.persistAndGetId(actividad, Long.class);
        entityManager.flush();
        entityManager.clear();

        ActividadAncestralMsp leida = entityManager.find(ActividadAncestralMsp.class, id);

        assertThat(leida.getNumeroIntento()).isEqualTo(1);
        assertThat(leida.getEstadoVerificacion()).isEqualTo(EstadoVerificacion.APROBADA);
        assertThat(leida.getSaberAncestral().getCategoria()).isEqualTo(CategoriaSaber.PREPARACION_TIERRA);
        assertThat(leida.getVerificadoPor().getCorreo()).isEqualTo("tecnico@msp.gob.ec");
        assertThat(leida.getParticipantes()).singleElement()
                .satisfies(p -> assertThat(p.getHorasTrabajadas()).isEqualTo(3));
    }

    @Test
    void guardaLaInsigniaOficialYElCalendarioLunar() {
        SaberAncestral saber = entityManager.persist(saber());
        MiembroInsignia insignia = new MiembroInsignia();
        insignia.setMiembro(miembro);
        insignia.setComunidad(comunidad);
        insignia.setTipoOrigen(TipoOrigenInsignia.OFICIAL_SABER_MSP);
        insignia.setNombreInsignia(saber.getNombreInsigniaOficial());
        insignia.setDescripcion("Preparó la tierra con su comunidad");
        insignia.setSaberAncestral(saber);
        insignia.setFechaObtencion(LocalDate.of(2026, 11, 10));
        Long insigniaId = entityManager.persistAndGetId(insignia, Long.class);

        CalendarioLunar fase = new CalendarioLunar();
        fase.setFaseLunar(FaseLunar.PURA_KILLA_LLENA);
        fase.setNombreKichwa("Pura Killa");
        fase.setNombreEspanol("Luna llena");
        fase.setFechaInicio(LocalDate.of(2026, 11, 24));
        fase.setFechaFin(LocalDate.of(2026, 12, 1));
        fase.setPracticasRecomendadas("Cosecha de hojas y flores");
        fase.setConsejoKinti("Cosecha en la mañana, cuando la planta guarda su fuerza");
        Long faseId = entityManager.persistAndGetId(fase, Long.class);
        entityManager.flush();
        entityManager.clear();

        MiembroInsignia insigniaLeida = entityManager.find(MiembroInsignia.class, insigniaId);
        CalendarioLunar faseLeida = entityManager.find(CalendarioLunar.class, faseId);

        assertThat(insigniaLeida.getTipoOrigen()).isEqualTo(TipoOrigenInsignia.OFICIAL_SABER_MSP);
        assertThat(insigniaLeida.getIconoCodigo()).isEqualTo("medalla-kinti");
        assertThat(insigniaLeida.getSaberAncestral().getOrdenPaso()).isEqualTo(1);
        assertThat(insigniaLeida.getTalentoComunidad()).isNull();
        assertThat(faseLeida.getFaseLunar()).isEqualTo(FaseLunar.PURA_KILLA_LLENA);
    }

    private Usuario responsable() {
        return usuario("rosa@correo.com", "1712345678", Rol.RESPONSABLE_COMUNIDAD);
    }

    private Usuario usuario(String correo, String identificacion, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto("Persona de prueba");
        usuario.setIdentificacion(identificacion);
        usuario.setFechaNacimiento(LocalDate.of(1990, 5, 14));
        usuario.setGenero(Genero.OTRO);
        usuario.setCorreo(correo);
        usuario.setPasswordHash("hash");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setCreadoEn(AHORA);
        return usuario;
    }

    private Comunidad comunidad(Usuario responsable) {
        Comunidad nueva = new Comunidad();
        nueva.setNombre("Hampi Chakra");
        nueva.setTipoOrganizacion(TipoOrganizacion.BARRIO);
        nueva.setProvincia("Pichincha");
        nueva.setParroquia("Calderón");
        nueva.setDireccion("Calle 1");
        nueva.setTelefonoContacto("0991234567");
        nueva.setCorreoContacto("contacto@correo.com");
        nueva.setResponsable(responsable);
        nueva.setActiva(true);
        nueva.setCreadoEn(AHORA);
        return nueva;
    }

    private Miembro miembro() {
        Miembro nuevo = new Miembro();
        nuevo.setCedula("1711111111");
        nuevo.setNombres("Ana");
        nuevo.setApellidos("Guamán");
        nuevo.setFechaNacimiento(LocalDate.of(2000, 1, 15));
        nuevo.setGenero(Genero.FEMENINO);
        nuevo.setNivel(NivelMiembro.APRENDIZ);
        nuevo.setConsentimientoDatos(true);
        nuevo.setConsentimientoEn(AHORA);
        nuevo.setActivo(true);
        nuevo.setCreadoEn(AHORA);
        return nuevo;
    }

    private Huerto huerto() {
        Huerto nuevo = new Huerto();
        nuevo.setComunidad(comunidad);
        nuevo.setNombreHuerto(comunidad.getNombre());
        nuevo.setLatitud(new BigDecimal("-0.0950000"));
        nuevo.setLongitud(new BigDecimal("-78.4220000"));
        nuevo.setAltitudMsnm(2800);
        nuevo.setDisenoPrincipal(DisenoPrincipal.EN_TIERRA);
        return nuevo;
    }

    private SaberAncestral saber() {
        SaberAncestral nuevo = new SaberAncestral();
        nuevo.setOrdenPaso(1);
        nuevo.setCodigoHito("PREP-TIERRA");
        nuevo.setTitulo("Preparación de la tierra");
        nuevo.setCategoria(CategoriaSaber.PREPARACION_TIERRA);
        nuevo.setFaseLunarIdeal(FaseLunar.WINAK_KILLA_CRECIENTE);
        nuevo.setHorarioEnergetico("05:00 a 07:00");
        nuevo.setContenidoEducativo("Se afloja la tierra con herramientas tradicionales.");
        nuevo.setNombreInsigniaOficial("Guardián de la Allpa");
        return nuevo;
    }
}
