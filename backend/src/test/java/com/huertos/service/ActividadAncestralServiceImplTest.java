package com.huertos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ActividadAncestralResponse;
import com.huertos.dto.ParticipanteActividadDto;
import com.huertos.dto.RegistrarActividadAncestralRequest;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.ActividadAncestralMapper;
import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.Comunidad;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.FaseLunar;
import com.huertos.model.Huerto;
import com.huertos.model.Miembro;
import com.huertos.model.SaberAncestral;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.ComunidadMiembroRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.SaberAncestralRepository;
import com.huertos.service.impl.ActividadAncestralServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActividadAncestralServiceImplTest {

    @Mock
    private ActividadAncestralMspRepository actividadAncestralRepository;

    @Mock
    private SaberAncestralRepository saberAncestralRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private HuertoRepository huertoRepository;

    @Mock
    private MiembroRepository miembroRepository;

    @Mock
    private ComunidadMiembroRepository comunidadMiembroRepository;

    @Spy
    private ActividadAncestralMapper actividadAncestralMapper = Mappers.getMapper(ActividadAncestralMapper.class);

    private ActividadAncestralServiceImpl actividadAncestralService;

    private Comunidad comunidad;
    private Huerto huerto;
    private SaberAncestral saber;
    private Miembro miembro;

    @BeforeEach
    void setUp() {
        actividadAncestralService = new ActividadAncestralServiceImpl(
                actividadAncestralRepository,
                saberAncestralRepository,
                comunidadRepository,
                huertoRepository,
                miembroRepository,
                comunidadMiembroRepository,
                actividadAncestralMapper
        );

        comunidad = new Comunidad();
        comunidad.setId(3L);
        comunidad.setNombre("Huerto Comunitario Allpa Viva");

        huerto = new Huerto();
        huerto.setId(5L);
        huerto.setComunidad(comunidad);

        saber = new SaberAncestral();
        saber.setId(1L);
        saber.setTitulo("Ritual de siembra");

        miembro = new Miembro();
        miembro.setId(7L);
        miembro.setNombres("María Tránsito");
        miembro.setApellidos("Amaguaña Alba");
    }

    @Test
    void registrarActividadGrande_guardaActividadPendienteConParticipantes() {
        RegistrarActividadAncestralRequest request = new RegistrarActividadAncestralRequest(
                1L,
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Ritual de siembra con chicha de jora y sahumerio",
                "https://evidencias.gob.ec/foto-ritual.jpg",
                "https://evidencias.gob.ec/video-ritual.mp4",
                List.of(new ParticipanteActividadDto(7L, 4))
        );

        when(comunidadRepository.findByResponsableCorreo("responsable@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(3L)).thenReturn(Optional.of(huerto));
        when(saberAncestralRepository.findById(1L)).thenReturn(Optional.of(saber));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(3L, 7L))
                .thenReturn(true);
        when(miembroRepository.findById(7L)).thenReturn(Optional.of(miembro));
        when(actividadAncestralRepository.save(any(ActividadAncestralMsp.class))).thenAnswer(inv -> {
            ActividadAncestralMsp entidad = inv.getArgument(0);
            entidad.setId(21L);
            return entidad;
        });

        ActividadAncestralResponse respuesta = actividadAncestralService
                .registrarActividadGrande("responsable@huertos.gob.ec", request);

        assertNotNull(respuesta);
        assertEquals(21L, respuesta.id());
        assertEquals(3L, respuesta.comunidadId());
        assertEquals("Ritual de siembra", respuesta.tituloSaber());
        assertEquals(EstadoVerificacion.PENDIENTE, respuesta.estado());
        assertEquals(1, respuesta.numeroIntento());
        assertEquals(LocalDate.of(2026, 11, 10), respuesta.fechaRealizacion());
        assertEquals(1, respuesta.participantes().size());
        assertEquals(7L, respuesta.participantes().get(0).miembroId());
        assertEquals(4, respuesta.participantes().get(0).horasTrabajadas());

        ArgumentCaptor<ActividadAncestralMsp> captor = ArgumentCaptor.forClass(ActividadAncestralMsp.class);
        verify(actividadAncestralRepository).save(captor.capture());
        ActividadAncestralMsp guardada = captor.getValue();
        assertEquals(EstadoVerificacion.PENDIENTE, guardada.getEstadoVerificacion());
        assertEquals(1, guardada.getNumeroIntento());
        assertEquals(comunidad, guardada.getComunidad());
        assertEquals(huerto, guardada.getHuerto());
        assertEquals(saber, guardada.getSaberAncestral());
        assertEquals(1, guardada.getParticipantes().size());
        assertEquals(4, guardada.getParticipantes().get(0).getHorasTrabajadas());
    }

    @Test
    void registrarActividadGrande_lanzaNoEncontradoSiResponsableNoTieneComunidad() {
        RegistrarActividadAncestralRequest request = new RegistrarActividadAncestralRequest(
                1L,
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Bitácora del ritual",
                "https://evidencias.gob.ec/foto-ritual.jpg",
                null,
                List.of(new ParticipanteActividadDto(7L, 4))
        );

        when(comunidadRepository.findByResponsableCorreo("solo@huertos.gob.ec"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> actividadAncestralService.registrarActividadGrande("solo@huertos.gob.ec", request));
        verify(actividadAncestralRepository, never()).save(any());
    }

    @Test
    void registrarActividadGrande_lanzaNoEncontradoSiSaberAncestralNoExiste() {
        RegistrarActividadAncestralRequest request = new RegistrarActividadAncestralRequest(
                99L,
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Bitácora del ritual",
                "https://evidencias.gob.ec/foto-ritual.jpg",
                null,
                List.of(new ParticipanteActividadDto(7L, 4))
        );

        when(comunidadRepository.findByResponsableCorreo("responsable@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(3L)).thenReturn(Optional.of(huerto));
        when(saberAncestralRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> actividadAncestralService.registrarActividadGrande("responsable@huertos.gob.ec", request));
        verify(actividadAncestralRepository, never()).save(any());
    }

    @Test
    void registrarActividadGrande_lanzaReglaNegocioSiParticipanteNoEsMiembroActivo() {
        RegistrarActividadAncestralRequest request = new RegistrarActividadAncestralRequest(
                1L,
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Bitácora del ritual",
                "https://evidencias.gob.ec/foto-ritual.jpg",
                null,
                List.of(new ParticipanteActividadDto(9L, 2))
        );

        when(comunidadRepository.findByResponsableCorreo("responsable@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(3L)).thenReturn(Optional.of(huerto));
        when(saberAncestralRepository.findById(1L)).thenReturn(Optional.of(saber));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(3L, 9L))
                .thenReturn(false);

        ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> actividadAncestralService.registrarActividadGrande("responsable@huertos.gob.ec", request));

        assertEquals("El participante seleccionado no es un miembro activo de la comunidad", ex.getMessage());
        verify(actividadAncestralRepository, never()).save(any());
    }

    @Test
    void registrarActividadGrande_lanzaReglaNegocioSiParticipanteSeRepite() {
        RegistrarActividadAncestralRequest request = new RegistrarActividadAncestralRequest(
                1L,
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Bitácora del ritual",
                "https://evidencias.gob.ec/foto-ritual.jpg",
                null,
                List.of(new ParticipanteActividadDto(7L, 4), new ParticipanteActividadDto(7L, 2))
        );

        when(comunidadRepository.findByResponsableCorreo("responsable@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(3L)).thenReturn(Optional.of(huerto));
        when(saberAncestralRepository.findById(1L)).thenReturn(Optional.of(saber));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(3L, 7L))
                .thenReturn(true);
        when(miembroRepository.findById(7L)).thenReturn(Optional.of(miembro));

        ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> actividadAncestralService.registrarActividadGrande("responsable@huertos.gob.ec", request));

        assertEquals("El participante ya está registrado en la actividad", ex.getMessage());
        verify(actividadAncestralRepository, never()).save(any());
    }
}
