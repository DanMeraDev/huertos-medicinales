package com.huertos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ActividadRevisionResponse;
import com.huertos.dto.ResultadoVerificacionResponse;
import com.huertos.dto.VerificarActividadMspRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.ActividadAncestralMapper;
import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.ActividadMspParticipante;
import com.huertos.model.Comunidad;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.FaseLunar;
import com.huertos.model.Huerto;
import com.huertos.model.Miembro;
import com.huertos.model.SaberAncestral;
import com.huertos.model.Usuario;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.MspRevisionServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MspRevisionServiceImplTest {

    @Mock
    private ActividadAncestralMspRepository actividadAncestralRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GamificacionService gamificacionService;

    @Spy
    private ActividadAncestralMapper actividadAncestralMapper = Mappers.getMapper(ActividadAncestralMapper.class);

    private MspRevisionServiceImpl mspRevisionService;

    private ActividadAncestralMsp actividad;
    private Usuario tecnico;

    @BeforeEach
    void setUp() {
        mspRevisionService = new MspRevisionServiceImpl(
                actividadAncestralRepository,
                usuarioRepository,
                gamificacionService,
                actividadAncestralMapper,
                Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneOffset.UTC)
        );

        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);
        comunidad.setNombre("Huerto Comunitario Allpa Viva");

        Huerto huerto = new Huerto();
        huerto.setId(5L);

        SaberAncestral saber = new SaberAncestral();
        saber.setId(1L);
        saber.setTitulo("Ritual de siembra");

        Miembro miembro = new Miembro();
        miembro.setId(7L);
        miembro.setNombres("María Tránsito");
        miembro.setApellidos("Amaguaña Alba");

        ActividadMspParticipante participante = new ActividadMspParticipante();
        participante.setMiembro(miembro);
        participante.setHorasTrabajadas(4);

        actividad = new ActividadAncestralMsp();
        actividad.setId(21L);
        actividad.setComunidad(comunidad);
        actividad.setHuerto(huerto);
        actividad.setSaberAncestral(saber);
        actividad.setFechaRealizacion(LocalDate.of(2026, 11, 10));
        actividad.setFaseLunarEjecucion(FaseLunar.PURA_KILLA_LLENA);
        actividad.setDescripcionBitacora("Ritual de siembra con chicha de jora y sahumerio");
        actividad.setEvidenciaFotoUrl("actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg");
        actividad.setNumeroIntento(1);
        actividad.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
        actividad.getParticipantes().add(participante);

        tecnico = new Usuario();
        tecnico.setId(42L);
        tecnico.setCorreo("tecnico@msp.gob.ec");
    }

    @Test
    void dictaminarActividad_apruebaRegistraTecnicoYDelegaLaAcreditacion() {
        when(actividadAncestralRepository.findById(21L)).thenReturn(Optional.of(actividad));
        when(usuarioRepository.findByCorreo("tecnico@msp.gob.ec")).thenReturn(Optional.of(tecnico));
        when(actividadAncestralRepository.save(any(ActividadAncestralMsp.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        doAnswer(invocacion -> {
            actividad.setPuntosAcreditarComunidad(20);
            return null;
        }).when(gamificacionService).procesarPuntosActividadMsp(21L);

        ResultadoVerificacionResponse respuesta = mspRevisionService.dictaminarActividad("tecnico@msp.gob.ec", 21L,
                new VerificarActividadMspRequest(EstadoVerificacion.APROBADA, null));

        assertEquals(21L, respuesta.actividadId());
        assertEquals(EstadoVerificacion.APROBADA, respuesta.estado());
        assertEquals(20, respuesta.puntosComunidadAcreditados());
        assertEquals(LocalDateTime.of(2026, 11, 12, 15, 0), respuesta.fechaVerificacion());
        assertEquals(EstadoVerificacion.APROBADA, actividad.getEstadoVerificacion());
        assertEquals(tecnico, actividad.getVerificadoPor());
        assertEquals(LocalDateTime.of(2026, 11, 12, 15, 0), actividad.getFechaVerificacion());
        verify(gamificacionService).procesarPuntosActividadMsp(21L);
    }

    @Test
    void dictaminarActividad_observaParaCorreccionGuardandoLaObservacion() {
        when(actividadAncestralRepository.findById(21L)).thenReturn(Optional.of(actividad));
        when(usuarioRepository.findByCorreo("tecnico@msp.gob.ec")).thenReturn(Optional.of(tecnico));
        when(actividadAncestralRepository.save(any(ActividadAncestralMsp.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        ResultadoVerificacionResponse respuesta = mspRevisionService.dictaminarActividad("tecnico@msp.gob.ec", 21L,
                new VerificarActividadMspRequest(EstadoVerificacion.OBSERVADA_CORREGIR,
                        "La fotografía no muestra el ritual completo"));

        assertEquals(EstadoVerificacion.OBSERVADA_CORREGIR, respuesta.estado());
        assertEquals(0, respuesta.puntosComunidadAcreditados());
        assertEquals("La fotografía no muestra el ritual completo", actividad.getObservacionMsp());
        verify(gamificacionService, never()).procesarPuntosActividadMsp(any());
    }

    @Test
    void dictaminarActividad_exigeObservacionParaObservarParaCorreccion() {
        when(actividadAncestralRepository.findById(21L)).thenReturn(Optional.of(actividad));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> mspRevisionService.dictaminarActividad("tecnico@msp.gob.ec", 21L,
                        new VerificarActividadMspRequest(EstadoVerificacion.OBSERVADA_CORREGIR, "  ")));

        assertEquals("Debe ingresar una observación detallada para justificar la observación o rechazo",
                ex.getMessage());
        verify(actividadAncestralRepository, never()).save(any());
        verify(gamificacionService, never()).procesarPuntosActividadMsp(any());
    }

    @Test
    void dictaminarActividad_exigeObservacionParaRechazarDefinitivamente() {
        when(actividadAncestralRepository.findById(21L)).thenReturn(Optional.of(actividad));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> mspRevisionService.dictaminarActividad("tecnico@msp.gob.ec", 21L,
                        new VerificarActividadMspRequest(EstadoVerificacion.RECHAZADA_DEFINITIVA, null)));

        assertEquals("Debe ingresar una observación detallada para justificar la observación o rechazo",
                ex.getMessage());
        verify(actividadAncestralRepository, never()).save(any());
    }

    @Test
    void dictaminarActividad_rechazaElDictamenPendiente() {
        when(actividadAncestralRepository.findById(21L)).thenReturn(Optional.of(actividad));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> mspRevisionService.dictaminarActividad("tecnico@msp.gob.ec", 21L,
                        new VerificarActividadMspRequest(EstadoVerificacion.PENDIENTE, null)));

        assertEquals("El dictamen debe ser APROBADA, OBSERVADA_CORREGIR o RECHAZADA_DEFINITIVA", ex.getMessage());
        verify(actividadAncestralRepository, never()).save(any());
    }

    @Test
    void dictaminarActividad_lanzaConflictoSiLaActividadYaFueDictaminada() {
        actividad.setEstadoVerificacion(EstadoVerificacion.APROBADA);
        when(actividadAncestralRepository.findById(21L)).thenReturn(Optional.of(actividad));

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> mspRevisionService.dictaminarActividad("tecnico@msp.gob.ec", 21L,
                        new VerificarActividadMspRequest(EstadoVerificacion.RECHAZADA_DEFINITIVA,
                                "No cumple los criterios agroecológicos")));

        assertEquals("La actividad ancestral ya fue dictaminada y no se encuentra en estado pendiente",
                ex.getMessage());
        verify(actividadAncestralRepository, never()).save(any());
    }

    @Test
    void dictaminarActividad_lanzaNoEncontradoSiLaActividadNoExiste() {
        when(actividadAncestralRepository.findById(21L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> mspRevisionService.dictaminarActividad("tecnico@msp.gob.ec", 21L,
                        new VerificarActividadMspRequest(EstadoVerificacion.APROBADA, null)));
    }

    @Test
    void listarPendientes_devuelveLasActividadesConSusEvidenciasYParticipantes() {
        when(actividadAncestralRepository.findByEstadoVerificacionOrderByCreadoEnAsc(EstadoVerificacion.PENDIENTE))
                .thenReturn(List.of(actividad));

        List<ActividadRevisionResponse> pendientes = mspRevisionService.listarPendientes();

        assertEquals(1, pendientes.size());
        ActividadRevisionResponse pendiente = pendientes.get(0);
        assertEquals(21L, pendiente.id());
        assertEquals(3L, pendiente.comunidadId());
        assertEquals("Huerto Comunitario Allpa Viva", pendiente.nombreComunidad());
        assertEquals("Ritual de siembra", pendiente.tituloSaber());
        assertEquals(LocalDate.of(2026, 11, 10), pendiente.fechaRealizacion());
        assertEquals(FaseLunar.PURA_KILLA_LLENA, pendiente.faseLunarEjecucion());
        assertEquals("Ritual de siembra con chicha de jora y sahumerio", pendiente.descripcionBitacora());
        assertEquals("actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg", pendiente.evidenciaFotoUrl());
        assertEquals(1, pendiente.numeroIntento());
        assertEquals(EstadoVerificacion.PENDIENTE, pendiente.estado());
        assertEquals(1, pendiente.participantes().size());
        assertEquals(7L, pendiente.participantes().get(0).miembroId());
        assertEquals("María Tránsito Amaguaña Alba", pendiente.participantes().get(0).nombreCompleto());
        assertEquals(4, pendiente.participantes().get(0).horasTrabajadas());
    }
}
