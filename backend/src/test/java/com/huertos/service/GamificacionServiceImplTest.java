package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.ActividadMspParticipante;
import com.huertos.model.Comunidad;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.Huerto;
import com.huertos.model.Miembro;
import com.huertos.model.MiembroInsignia;
import com.huertos.model.NivelMiembro;
import com.huertos.model.SaberAncestral;
import com.huertos.model.TipoOrigenInsignia;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
import com.huertos.repository.RegistroActividadMiembroRepository;
import com.huertos.repository.TalentoComunidadRepository;
import com.huertos.service.impl.GamificacionServiceImpl;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GamificacionServiceImplTest {

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private HuertoRepository huertoRepository;

    @Mock
    private MiembroRepository miembroRepository;

    @Mock
    private ActividadAncestralMspRepository actividadMspRepository;

    @Mock
    private MiembroInsigniaRepository miembroInsigniaRepository;

    @Mock
    private RegistroActividadMiembroRepository registroActividadMiembroRepository;

    @Mock
    private TalentoComunidadRepository talentoComunidadRepository;

    @InjectMocks
    private GamificacionServiceImpl gamificacionService;

    private Comunidad comunidad;
    private Huerto huerto;
    private SaberAncestral saberAncestral;
    private ActividadAncestralMsp actividad;
    private Miembro miembro1;
    private Miembro miembro2;
    private ActividadMspParticipante participante1;
    private ActividadMspParticipante participante2;

    @BeforeEach
    void setUp() {
        comunidad = new Comunidad();
        comunidad.setId(1L);
        comunidad.setNombre("Comunidad San Francisco");
        comunidad.setPuntosComunidad(50);
        comunidad.setActiva(true);

        huerto = new Huerto();
        huerto.setId(10L);
        huerto.setNombreHuerto("Huerto Comunitario Central");
        huerto.setPuntosHuerto(30);
        huerto.setComunidad(comunidad);

        saberAncestral = new SaberAncestral();
        saberAncestral.setId(100L);
        saberAncestral.setTitulo("Preparación de Biol Ancestral");
        saberAncestral.setPuntosBaseComunidad(20);
        saberAncestral.setNombreInsigniaOficial("Guardián del Biol");

        miembro1 = new Miembro();
        miembro1.setId(101L);
        miembro1.setNombres("Rosa");
        miembro1.setApellidos("Quishpe");
        miembro1.setNivel(NivelMiembro.APRENDIZ);
        miembro1.setPuntosTotales(10);
        miembro1.setActivo(true);

        miembro2 = new Miembro();
        miembro2.setId(102L);
        miembro2.setNombres("Manuel");
        miembro2.setApellidos("Toapanta");
        miembro2.setNivel(NivelMiembro.APRENDIZ);
        miembro2.setPuntosTotales(45);
        miembro2.setActivo(true);

        actividad = new ActividadAncestralMsp();
        actividad.setId(1L);
        actividad.setComunidad(comunidad);
        actividad.setHuerto(huerto);
        actividad.setSaberAncestral(saberAncestral);
        actividad.setNumeroIntento(1);
        actividad.setEstadoVerificacion(EstadoVerificacion.APROBADA);

        participante1 = new ActividadMspParticipante();
        participante1.setId(201L);
        participante1.setActividadMsp(actividad);
        participante1.setMiembro(miembro1);
        participante1.setHorasTrabajadas(3);

        participante2 = new ActividadMspParticipante();
        participante2.setId(202L);
        participante2.setActividadMsp(actividad);
        participante2.setMiembro(miembro2);
        participante2.setHorasTrabajadas(6);

        List<ActividadMspParticipante> participantes = new ArrayList<>();
        participantes.add(participante1);
        participantes.add(participante2);
        actividad.setParticipantes(participantes);
    }

    @Test
    @DisplayName("Intento 1: Acredita el 100% de puntos a comunidad y huerto, y 1 punto por hora a participantes")
    void procesarPuntosActividadMsp_intento1_acredita100PorCientoYHoras() {
        when(actividadMspRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(miembroRepository.findById(101L)).thenReturn(Optional.of(miembro1));
        when(miembroRepository.findById(102L)).thenReturn(Optional.of(miembro2));

        gamificacionService.procesarPuntosActividadMsp(1L);

        // Comunidad: 50 + 20 = 70
        assertThat(comunidad.getPuntosComunidad()).isEqualTo(70);
        // Huerto: 30 + 20 = 50
        assertThat(huerto.getPuntosHuerto()).isEqualTo(50);
        // Actividad registra los puntos acreditados a la comunidad
        assertThat(actividad.getPuntosAcreditarComunidad()).isEqualTo(20);

        // Participante 1: 3 horas = 3 puntos; miembro1: 10 + 3 = 13 (sigue APRENDIZ)
        assertThat(participante1.getPuntosAcreditados()).isEqualTo(3);
        assertThat(miembro1.getPuntosTotales()).isEqualTo(13);
        assertThat(miembro1.getNivel()).isEqualTo(NivelMiembro.APRENDIZ);

        // Participante 2: 6 horas = 6 puntos; miembro2: 45 + 6 = 51 (asciende a OFICIAL)
        assertThat(participante2.getPuntosAcreditados()).isEqualTo(6);
        assertThat(miembro2.getPuntosTotales()).isEqualTo(51);
        assertThat(miembro2.getNivel()).isEqualTo(NivelMiembro.OFICIAL);

        verify(comunidadRepository).save(comunidad);
        verify(huertoRepository).save(huerto);
        verify(miembroRepository).save(miembro1);
        verify(miembroRepository).save(miembro2);
        verify(actividadMspRepository).save(actividad);
    }

    @Test
    @DisplayName("Intento 2: Aplica penalización del 70% a comunidad y huerto (14 puntos)")
    void procesarPuntosActividadMsp_intento2_aplicaPenalizacion70PorCiento() {
        actividad.setNumeroIntento(2);
        when(actividadMspRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(miembroRepository.findById(101L)).thenReturn(Optional.of(miembro1));
        when(miembroRepository.findById(102L)).thenReturn(Optional.of(miembro2));

        gamificacionService.procesarPuntosActividadMsp(1L);

        // 70% de 20 = 14
        // Comunidad: 50 + 14 = 64
        assertThat(comunidad.getPuntosComunidad()).isEqualTo(64);
        // Huerto: 30 + 14 = 44
        assertThat(huerto.getPuntosHuerto()).isEqualTo(44);
        assertThat(actividad.getPuntosAcreditarComunidad()).isEqualTo(14);

        verify(comunidadRepository).save(comunidad);
        verify(huertoRepository).save(huerto);
    }

    @Test
    @DisplayName("Lanza ReglaNegocioException si la actividad no está en estado APROBADA")
    void procesarPuntosActividadMsp_lanzaExcepcion_cuandoNoEstaAprobada() {
        actividad.setEstadoVerificacion(EstadoVerificacion.PENDIENTE);
        when(actividadMspRepository.findById(1L)).thenReturn(Optional.of(actividad));

        assertThatThrownBy(() -> gamificacionService.procesarPuntosActividadMsp(1L))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("No se pueden otorgar puntos de gamificación en actividades no aprobadas");

        verify(comunidadRepository, never()).save(any());
        verify(huertoRepository, never()).save(any());
        verify(miembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanza RecursoNoEncontradoException si la actividad no existe")
    void procesarPuntosActividadMsp_lanzaExcepcion_cuandoActividadNoExiste() {
        when(actividadMspRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gamificacionService.procesarPuntosActividadMsp(999L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(comunidadRepository, never()).save(any());
    }

    @Test
    @DisplayName("Adjudica insignia oficial a participantes que no la tenían y omite duplicados")
    void procesarPuntosActividadMsp_adjudicaInsigniasOficiales_sinDuplicados() {
        when(actividadMspRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(miembroRepository.findById(101L)).thenReturn(Optional.of(miembro1));
        when(miembroRepository.findById(102L)).thenReturn(Optional.of(miembro2));

        // Miembro 1 no tiene la insignia
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(101L, 100L)).thenReturn(false);
        // Miembro 2 ya posee la insignia
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(102L, 100L)).thenReturn(true);

        gamificacionService.procesarPuntosActividadMsp(1L);

        ArgumentCaptor<MiembroInsignia> insigniaCaptor = ArgumentCaptor.forClass(MiembroInsignia.class);
        verify(miembroInsigniaRepository).save(insigniaCaptor.capture());

        MiembroInsignia insigniaGuardada = insigniaCaptor.getValue();
        assertThat(insigniaGuardada.getMiembro().getId()).isEqualTo(101L);
        assertThat(insigniaGuardada.getComunidad().getId()).isEqualTo(1L);
        assertThat(insigniaGuardada.getTipoOrigen()).isEqualTo(TipoOrigenInsignia.OFICIAL_SABER_MSP);
        assertThat(insigniaGuardada.getNombreInsignia()).isEqualTo("Guardián del Biol");
        assertThat(insigniaGuardada.getDescripcion()).contains("Preparación de Biol Ancestral");
        assertThat(insigniaGuardada.getIconoCodigo()).isEqualTo("medalla-kinti");
        assertThat(insigniaGuardada.getFechaObtencion()).isEqualTo(LocalDate.now());
        assertThat(insigniaGuardada.getSaberAncestral()).isEqualTo(saberAncestral);
    }

    @Test
    @DisplayName("Miembro inactivo: registra puntos ganados sin emitir error")
    void procesarPuntosActividadMsp_miembroInactivo_registraPuntosCorrectamente() {
        miembro1.setActivo(false);
        when(actividadMspRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(miembroRepository.findById(101L)).thenReturn(Optional.of(miembro1));
        when(miembroRepository.findById(102L)).thenReturn(Optional.of(miembro2));

        gamificacionService.procesarPuntosActividadMsp(1L);

        assertThat(participante1.getPuntosAcreditados()).isEqualTo(3);
        assertThat(miembro1.getPuntosTotales()).isEqualTo(13);
        verify(miembroRepository).save(miembro1);
    }

    @Test
    @DisplayName("evaluarAscensoNivel: asciende a OFICIAL con 50 puntos y a MAESTRO con 150 puntos")
    void evaluarAscensoNivel_evaluaRangosCorrectamente() {
        Miembro aprendiz = new Miembro();
        aprendiz.setId(1L);
        aprendiz.setPuntosTotales(49);
        aprendiz.setNivel(NivelMiembro.APRENDIZ);
        when(miembroRepository.findById(1L)).thenReturn(Optional.of(aprendiz));

        assertThat(gamificacionService.evaluarAscensoNivel(1L)).isEqualTo(NivelMiembro.APRENDIZ);

        aprendiz.setPuntosTotales(50);
        assertThat(gamificacionService.evaluarAscensoNivel(1L)).isEqualTo(NivelMiembro.OFICIAL);
        assertThat(aprendiz.getNivel()).isEqualTo(NivelMiembro.OFICIAL);

        Miembro oficial = new Miembro();
        oficial.setId(2L);
        oficial.setPuntosTotales(150);
        oficial.setNivel(NivelMiembro.OFICIAL);
        when(miembroRepository.findById(2L)).thenReturn(Optional.of(oficial));

        assertThat(gamificacionService.evaluarAscensoNivel(2L)).isEqualTo(NivelMiembro.MAESTRO);
        assertThat(oficial.getNivel()).isEqualTo(NivelMiembro.MAESTRO);

        Miembro maestro = new Miembro();
        maestro.setId(3L);
        maestro.setPuntosTotales(200);
        maestro.setNivel(NivelMiembro.MAESTRO);
        when(miembroRepository.findById(3L)).thenReturn(Optional.of(maestro));

        assertThat(gamificacionService.evaluarAscensoNivel(3L)).isEqualTo(NivelMiembro.MAESTRO);
        assertThat(maestro.getNivel()).isEqualTo(NivelMiembro.MAESTRO);
    }

    @Test
    @DisplayName("otorgarInsigniaOficial: adjudica insignia oficial a participantes elegibles")
    void otorgarInsigniaOficial_creaInsigniasCorrectas() {
        when(actividadMspRepository.findById(1L)).thenReturn(Optional.of(actividad));
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(101L, 100L)).thenReturn(false);
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(102L, 100L)).thenReturn(false);

        gamificacionService.otorgarInsigniaOficial(1L);

        ArgumentCaptor<MiembroInsignia> captor = ArgumentCaptor.forClass(MiembroInsignia.class);
        verify(miembroInsigniaRepository, org.mockito.Mockito.times(2)).save(captor.capture());

        List<MiembroInsignia> insignias = captor.getAllValues();
        assertThat(insignias).hasSize(2);
        assertThat(insignias.get(0).getNombreInsignia()).isEqualTo("Guardián del Biol");
        assertThat(insignias.get(0).getTipoOrigen()).isEqualTo(TipoOrigenInsignia.OFICIAL_SABER_MSP);
    }
}
