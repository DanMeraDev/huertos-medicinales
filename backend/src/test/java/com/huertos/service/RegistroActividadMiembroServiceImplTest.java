package com.huertos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.RegistrarLaborMiembroRequest;
import com.huertos.dto.ResultadoAcreditacionMiembroResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.CategoriaActividad;
import com.huertos.model.Comunidad;
import com.huertos.model.Miembro;
import com.huertos.model.MiembroInsignia;
import com.huertos.model.NivelMiembro;
import com.huertos.model.RegistroActividadMiembro;
import com.huertos.model.TalentoComunidad;
import com.huertos.repository.ActividadComunidadRepository;
import com.huertos.repository.ComunidadMiembroRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.RegistroActividadMiembroRepository;
import com.huertos.repository.TalentoComunidadRepository;
import com.huertos.service.impl.RegistroActividadMiembroServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistroActividadMiembroServiceImplTest {

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private ComunidadMiembroRepository comunidadMiembroRepository;

    @Mock
    private MiembroRepository miembroRepository;

    @Mock
    private ActividadComunidadRepository actividadComunidadRepository;

    @Mock
    private RegistroActividadMiembroRepository registroActividadMiembroRepository;

    @Mock
    private TalentoComunidadRepository talentoComunidadRepository;

    @Mock
    private MiembroInsigniaRepository miembroInsigniaRepository;

    @InjectMocks
    private RegistroActividadMiembroServiceImpl service;

    private Comunidad comunidad;
    private Miembro miembro;
    private ActividadComunidad actividad;

    @BeforeEach
    void setUp() {
        comunidad = new Comunidad();
        comunidad.setId(10L);
        comunidad.setNombre("Comunidad Allpa");
        comunidad.setPuntosComunidad(100);
        comunidad.setActiva(true);

        miembro = new Miembro();
        miembro.setId(25L);
        miembro.setCedula("1712345678");
        miembro.setNombres("Inti");
        miembro.setApellidos("Quishpe");
        miembro.setNivel(NivelMiembro.APRENDIZ);
        miembro.setPuntosTotales(20);
        miembro.setActivo(true);

        actividad = new ActividadComunidad();
        actividad.setId(5L);
        actividad.setComunidad(comunidad);
        actividad.setNombre("Deshierbe de terrazas");
        actividad.setCategoria(CategoriaActividad.LIMPIEZA_DESHIERBE);
        actividad.setPuntosPorHora(1);
        actividad.setActiva(true);
    }

    @Test
    void registrarLabor_exitoso_acreditaPuntosSoloAMiembro_sinAscensoNivel() {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                4,
                "Jornada matutina en terrazas"
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(miembroRepository.findById(25L))
                .thenReturn(Optional.of(miembro));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(10L, 25L))
                .thenReturn(true);
        when(actividadComunidadRepository.findById(5L))
                .thenReturn(Optional.of(actividad));
        when(registroActividadMiembroRepository.save(any(RegistroActividadMiembro.class)))
                .thenAnswer(inv -> {
                    RegistroActividadMiembro reg = inv.getArgument(0);
                    reg.setId(99L);
                    return reg;
                });
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(5L))
                .thenReturn(List.of());

        ResultadoAcreditacionMiembroResponse respuesta = service.registrarLabor("resp@huertos.gob.ec", request);

        assertNotNull(respuesta);
        assertEquals(99L, respuesta.registroId());
        assertEquals(25L, respuesta.miembroId());
        assertEquals(4, respuesta.puntosOtorgados());
        assertEquals(24, respuesta.puntosTotales());
        assertEquals(NivelMiembro.APRENDIZ, respuesta.nivel());
        assertFalse(respuesta.subioDeNivel());
        assertTrue(respuesta.talentosDesbloqueados().isEmpty());

        assertEquals(100, comunidad.getPuntosComunidad());

        ArgumentCaptor<RegistroActividadMiembro> captor = ArgumentCaptor.forClass(RegistroActividadMiembro.class);
        verify(registroActividadMiembroRepository).save(captor.capture());
        RegistroActividadMiembro guardado = captor.getValue();
        assertEquals(4, guardado.getHorasTrabajadas());
        assertEquals(4, guardado.getPuntosOtorgados());
        assertEquals("Jornada matutina en terrazas", guardado.getObservacion());
    }

    @Test
    void registrarLabor_exitoso_asciendeNivel_deAprendizAOficial() {
        miembro.setPuntosTotales(48);

        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                4,
                null
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(miembroRepository.findById(25L))
                .thenReturn(Optional.of(miembro));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(10L, 25L))
                .thenReturn(true);
        when(actividadComunidadRepository.findById(5L))
                .thenReturn(Optional.of(actividad));
        when(registroActividadMiembroRepository.save(any(RegistroActividadMiembro.class)))
                .thenAnswer(inv -> {
                    RegistroActividadMiembro reg = inv.getArgument(0);
                    reg.setId(100L);
                    return reg;
                });
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(5L))
                .thenReturn(List.of());

        ResultadoAcreditacionMiembroResponse respuesta = service.registrarLabor("resp@huertos.gob.ec", request);

        assertNotNull(respuesta);
        assertEquals(52, respuesta.puntosTotales());
        assertEquals(NivelMiembro.OFICIAL, respuesta.nivel());
        assertTrue(respuesta.subioDeNivel());
        assertEquals(NivelMiembro.OFICIAL, miembro.getNivel());
    }

    @Test
    void registrarLabor_exitoso_asciendeNivel_deOficialAMaestro() {
        miembro.setPuntosTotales(145);
        miembro.setNivel(NivelMiembro.OFICIAL);

        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                8,
                "Abonado integral"
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(miembroRepository.findById(25L))
                .thenReturn(Optional.of(miembro));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(10L, 25L))
                .thenReturn(true);
        when(actividadComunidadRepository.findById(5L))
                .thenReturn(Optional.of(actividad));
        when(registroActividadMiembroRepository.save(any(RegistroActividadMiembro.class)))
                .thenAnswer(inv -> {
                    RegistroActividadMiembro reg = inv.getArgument(0);
                    reg.setId(101L);
                    return reg;
                });
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(5L))
                .thenReturn(List.of());

        ResultadoAcreditacionMiembroResponse respuesta = service.registrarLabor("resp@huertos.gob.ec", request);

        assertNotNull(respuesta);
        assertEquals(153, respuesta.puntosTotales());
        assertEquals(NivelMiembro.MAESTRO, respuesta.nivel());
        assertTrue(respuesta.subioDeNivel());
    }

    @Test
    void registrarLabor_desbloqueaTalentoComunitario_cuandoAlcanzaMeta() {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                2,
                "Compostaje"
        );

        TalentoComunidad talento = new TalentoComunidad();
        talento.setId(77L);
        talento.setComunidad(comunidad);
        talento.setActividadComunidad(actividad);
        talento.setNombreInsignia("Guardián del Compost");
        talento.setDescripcion("Realizó 3 labores de compostaje");
        talento.setMetaRepeticiones(3);
        talento.setIconoCodigo("insignia-semilla");
        talento.setActivo(true);

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(miembroRepository.findById(25L))
                .thenReturn(Optional.of(miembro));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(10L, 25L))
                .thenReturn(true);
        when(actividadComunidadRepository.findById(5L))
                .thenReturn(Optional.of(actividad));
        when(registroActividadMiembroRepository.save(any(RegistroActividadMiembro.class)))
                .thenAnswer(inv -> {
                    RegistroActividadMiembro reg = inv.getArgument(0);
                    reg.setId(102L);
                    return reg;
                });
        when(registroActividadMiembroRepository.countByMiembroIdAndActividadComunidadId(25L, 5L))
                .thenReturn(3L);
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(5L))
                .thenReturn(List.of(talento));
        when(miembroInsigniaRepository.existsByMiembroIdAndTalentoComunidadId(25L, 77L))
                .thenReturn(false);

        ResultadoAcreditacionMiembroResponse respuesta = service.registrarLabor("resp@huertos.gob.ec", request);

        assertNotNull(respuesta);
        assertEquals(1, respuesta.talentosDesbloqueados().size());
        assertEquals("Guardián del Compost", respuesta.talentosDesbloqueados().get(0));

        verify(miembroInsigniaRepository).save(any(MiembroInsignia.class));
    }

    @Test
    void registrarLabor_falla_cuandoMiembroNoActivoEnComunidad() {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                2,
                null
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(miembroRepository.findById(25L))
                .thenReturn(Optional.of(miembro));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(10L, 25L))
                .thenReturn(false);

        assertThrows(ReglaNegocioException.class,
                () -> service.registrarLabor("resp@huertos.gob.ec", request));

        verify(registroActividadMiembroRepository, never()).save(any());
    }

    @Test
    void registrarLabor_falla_cuandoActividadNoPerteneceAComunidad() {
        Comunidad otraComunidad = new Comunidad();
        otraComunidad.setId(999L);
        actividad.setComunidad(otraComunidad);

        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                2,
                null
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(miembroRepository.findById(25L))
                .thenReturn(Optional.of(miembro));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(10L, 25L))
                .thenReturn(true);
        when(actividadComunidadRepository.findById(5L))
                .thenReturn(Optional.of(actividad));

        assertThrows(ReglaNegocioException.class,
                () -> service.registrarLabor("resp@huertos.gob.ec", request));

        verify(registroActividadMiembroRepository, never()).save(any());
    }

    @Test
    void registrarLabor_falla_cuandoComunidadNoExiste() {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                2,
                null
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.registrarLabor("resp@huertos.gob.ec", request));
    }

    @Test
    void listarInsignias_responde404_cuandoElMiembroEsDeOtraComunidad() {
        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(comunidadMiembroRepository.existsByComunidadIdAndMiembroIdAndActivoEnComunidadTrue(10L, 99L))
                .thenReturn(false);

        RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
                () -> service.listarInsigniasMiembro("resp@huertos.gob.ec", 99L));

        assertEquals("El miembro no pertenece a la comunidad del usuario", error.getMessage());
    }
}
