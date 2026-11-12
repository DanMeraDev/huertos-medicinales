package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.CrearTalentoComunidadRequest;
import com.huertos.dto.TalentoComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.TalentoComunidadMapper;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.CategoriaActividad;
import com.huertos.model.Comunidad;
import com.huertos.model.TalentoComunidad;
import com.huertos.model.Usuario;
import com.huertos.repository.ActividadComunidadRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.TalentoComunidadRepository;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.impl.TalentoComunidadServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
class TalentoComunidadServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-11-12T10:00:00Z");
    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");

    @Mock
    private TalentoComunidadRepository talentoComunidadRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private ActividadComunidadRepository actividadComunidadRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private TalentoComunidadMapper talentoComunidadMapper = Mappers.getMapper(TalentoComunidadMapper.class);

    private Clock clock;
    private TalentoComunidadService talentoComunidadService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(AHORA, ZONA);
        talentoComunidadService = new TalentoComunidadServiceImpl(
                talentoComunidadRepository,
                comunidadRepository,
                actividadComunidadRepository,
                usuarioRepository,
                talentoComunidadMapper,
                clock
        );
    }

    @Test
    void registrarTalentoCreaYRetornaTalentoExitosamente() {
        Long responsableId = 10L;
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Maestro del Abonaje 1",
                "Otorgada al miembro voluntario que complete 50 sesiones de preparación de compost",
                50
        );

        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);

        ActividadComunidad actividad = new ActividadComunidad();
        actividad.setId(2L);
        actividad.setNombre("Abonar camas de siembra");
        actividad.setComunidad(comunidad);
        actividad.setCategoria(CategoriaActividad.ABONO_COMPOST);

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(actividadComunidadRepository.findById(2L)).thenReturn(Optional.of(actividad));
        when(talentoComunidadRepository.existsByComunidadIdAndNombreInsigniaAndActivoTrue(3L, "Maestro del Abonaje 1"))
                .thenReturn(false);

        when(talentoComunidadRepository.save(any(TalentoComunidad.class))).thenAnswer(invocation -> {
            TalentoComunidad t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        TalentoComunidadResponse response = talentoComunidadService.registrarTalento(responsableId, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.comunidadId()).isEqualTo(3L);
        assertThat(response.actividadComunidadId()).isEqualTo(2L);
        assertThat(response.nombreActividad()).isEqualTo("Abonar camas de siembra");
        assertThat(response.nombreInsignia()).isEqualTo("Maestro del Abonaje 1");
        assertThat(response.metaRepeticiones()).isEqualTo(50);
        assertThat(response.iconoCodigo()).isEqualTo("insignia-semilla");
        assertThat(response.activo()).isTrue();
        assertThat(response.creadoEn()).isEqualTo(LocalDateTime.now(clock));

        ArgumentCaptor<TalentoComunidad> captor = ArgumentCaptor.forClass(TalentoComunidad.class);
        verify(talentoComunidadRepository).save(captor.capture());
        TalentoComunidad guardado = captor.getValue();
        assertThat(guardado.getIconoCodigo()).isEqualTo("insignia-semilla");
        assertThat(guardado.isActivo()).isTrue();
    }

    @Test
    void registrarTalentoLanzaConflictoSiNombreInsigniaYaExisteEnComunidad() {
        Long responsableId = 10L;
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Maestro del Abonaje 1",
                "Descripción",
                50
        );

        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);

        ActividadComunidad actividad = new ActividadComunidad();
        actividad.setId(2L);
        actividad.setComunidad(comunidad);

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(actividadComunidadRepository.findById(2L)).thenReturn(Optional.of(actividad));
        when(talentoComunidadRepository.existsByComunidadIdAndNombreInsigniaAndActivoTrue(3L, "Maestro del Abonaje 1"))
                .thenReturn(true);

        assertThatThrownBy(() -> talentoComunidadService.registrarTalento(responsableId, request))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un talento o medalla registrada con este nombre en tu comunidad");

        verify(talentoComunidadRepository, never()).save(any());
    }

    @Test
    void registrarTalentoLanzaRecursoNoEncontradoSiActividadNoExisteONoPerteneceAComunidad() {
        Long responsableId = 10L;
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                99L,
                "Maestro del Riego",
                "Descripción",
                20
        );

        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(actividadComunidadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> talentoComunidadService.registrarTalento(responsableId, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La actividad comunitaria especificada no existe o no pertenece a tu comunidad");

        verify(talentoComunidadRepository, never()).save(any());
    }

    @Test
    void registrarTalentoLanzaRecursoNoEncontradoSiActividadPerteneceAOtraComunidad() {
        Long responsableId = 10L;
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                5L,
                "Maestro del Riego",
                "Descripción",
                20
        );

        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);

        Comunidad otraComunidad = new Comunidad();
        otraComunidad.setId(8L);

        ActividadComunidad actividadOtraComunidad = new ActividadComunidad();
        actividadOtraComunidad.setId(5L);
        actividadOtraComunidad.setComunidad(otraComunidad);

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(actividadComunidadRepository.findById(5L)).thenReturn(Optional.of(actividadOtraComunidad));

        assertThatThrownBy(() -> talentoComunidadService.registrarTalento(responsableId, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La actividad comunitaria especificada no existe o no pertenece a tu comunidad");

        verify(talentoComunidadRepository, never()).save(any());
    }

    @Test
    void registrarTalentoLanzaRecursoNoEncontradoSiResponsableNoTieneComunidad() {
        Long responsableId = 99L;
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Medalla Semilla",
                "Descripción",
                10
        );

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> talentoComunidadService.registrarTalento(responsableId, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró ninguna comunidad asociada al responsable autenticado");
    }

    @Test
    void listarPorResponsableRetornaTalentosActivosDeComunidad() {
        Long responsableId = 10L;

        Comunidad comunidad = new Comunidad();
        comunidad.setId(3L);

        ActividadComunidad actividad = new ActividadComunidad();
        actividad.setId(2L);
        actividad.setNombre("Abonar");
        actividad.setComunidad(comunidad);

        TalentoComunidad talento = new TalentoComunidad();
        talento.setId(1L);
        talento.setComunidad(comunidad);
        talento.setActividadComunidad(actividad);
        talento.setNombreInsignia("Maestro del Abonaje");
        talento.setDescripcion("Descripción");
        talento.setMetaRepeticiones(50);
        talento.setIconoCodigo("insignia-semilla");
        talento.setActivo(true);
        talento.setCreadoEn(LocalDateTime.now(clock));

        when(comunidadRepository.findByResponsableId(responsableId)).thenReturn(Optional.of(comunidad));
        when(talentoComunidadRepository.findByComunidadIdAndActivoTrue(3L)).thenReturn(List.of(talento));

        List<TalentoComunidadResponse> listado = talentoComunidadService.listarPorResponsable(responsableId);

        assertThat(listado).hasSize(1);
        assertThat(listado.getFirst().nombreInsignia()).isEqualTo("Maestro del Abonaje");
        assertThat(listado.getFirst().iconoCodigo()).isEqualTo("insignia-semilla");
    }

    @Test
    void registrarTalentoPorCorreoLanzaRecursoNoEncontradoSiUsuarioNoExiste() {
        String correo = "desconocido@huertos.gob.ec";
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Nombre",
                "Descripción",
                10
        );

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> talentoComunidadService.registrarTalentoPorCorreo(correo, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No se encontró el usuario");
    }
}
