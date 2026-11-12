package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.model.ActividadAncestralMsp;
import com.huertos.model.ActividadMspParticipante;
import com.huertos.model.Comunidad;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.Miembro;
import com.huertos.model.MiembroInsignia;
import com.huertos.model.SaberAncestral;
import com.huertos.model.TipoOrigenInsignia;
import com.huertos.repository.ActividadAncestralMspRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.MiembroInsigniaRepository;
import com.huertos.repository.MiembroRepository;
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
class InsigniasOficialesTest {

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

    @InjectMocks
    private GamificacionServiceImpl gamificacionService;

    private Comunidad comunidad;
    private SaberAncestral saberAncestral;
    private ActividadAncestralMsp actividad;
    private Miembro miembro1;
    private Miembro miembro2;
    private ActividadMspParticipante participante1;
    private ActividadMspParticipante participante2;

    @BeforeEach
    void setUp() {
        comunidad = new Comunidad();
        comunidad.setId(10L);
        comunidad.setNombre("Comunidad La Esperanza");

        saberAncestral = new SaberAncestral();
        saberAncestral.setId(100L);
        saberAncestral.setTitulo("Conservación de Semillas Ancestrales");
        saberAncestral.setNombreInsigniaOficial("Guardián de la Semilla");

        miembro1 = new Miembro();
        miembro1.setId(1L);
        miembro1.setNombres("Carmen");
        miembro1.setApellidos("Chuquimarca");

        miembro2 = new Miembro();
        miembro2.setId(2L);
        miembro2.setNombres("Luis");
        miembro2.setApellidos("Guamán");

        actividad = new ActividadAncestralMsp();
        actividad.setId(50L);
        actividad.setComunidad(comunidad);
        actividad.setSaberAncestral(saberAncestral);
        actividad.setEstadoVerificacion(EstadoVerificacion.APROBADA);

        participante1 = new ActividadMspParticipante();
        participante1.setId(501L);
        participante1.setActividadMsp(actividad);
        participante1.setMiembro(miembro1);
        participante1.setHorasTrabajadas(4);

        participante2 = new ActividadMspParticipante();
        participante2.setId(502L);
        participante2.setActividadMsp(actividad);
        participante2.setMiembro(miembro2);
        participante2.setHorasTrabajadas(6);

        List<ActividadMspParticipante> participantes = new ArrayList<>();
        participantes.add(participante1);
        participantes.add(participante2);
        actividad.setParticipantes(participantes);
    }

    @Test
    @DisplayName("Crea MiembroInsignia con origen OFICIAL_SABER_MSP y atributos correctos para participantes elegibles")
    void otorgarInsigniaOficial_participantesElegibles_creaInsigniasCorrectas() {
        when(actividadMspRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(1L, 100L)).thenReturn(false);
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(2L, 100L)).thenReturn(false);

        gamificacionService.otorgarInsigniaOficial(50L);

        ArgumentCaptor<MiembroInsignia> captor = ArgumentCaptor.forClass(MiembroInsignia.class);
        verify(miembroInsigniaRepository, times(2)).save(captor.capture());

        List<MiembroInsignia> insignias = captor.getAllValues();
        assertThat(insignias).hasSize(2);

        MiembroInsignia insignia1 = insignias.get(0);
        assertThat(insignia1.getMiembro().getId()).isEqualTo(1L);
        assertThat(insignia1.getComunidad().getId()).isEqualTo(10L);
        assertThat(insignia1.getTipoOrigen()).isEqualTo(TipoOrigenInsignia.OFICIAL_SABER_MSP);
        assertThat(insignia1.getNombreInsignia()).isEqualTo("Guardián de la Semilla");
        assertThat(insignia1.getDescripcion()).isEqualTo("Insignia oficial obtenida por completar el hito: Conservación de Semillas Ancestrales");
        assertThat(insignia1.getIconoCodigo()).isEqualTo("medalla-kinti");
        assertThat(insignia1.getFechaObtencion()).isEqualTo(LocalDate.now());
        assertThat(insignia1.getSaberAncestral()).isEqualTo(saberAncestral);

        MiembroInsignia insignia2 = insignias.get(1);
        assertThat(insignia2.getMiembro().getId()).isEqualTo(2L);
        assertThat(insignia2.getNombreInsignia()).isEqualTo("Guardián de la Semilla");
        assertThat(insignia2.getTipoOrigen()).isEqualTo(TipoOrigenInsignia.OFICIAL_SABER_MSP);
    }

    @Test
    @DisplayName("Omite inserción duplicada si el miembro ya poseía la insignia oficial de este hito")
    void otorgarInsigniaOficial_miembroYaPoseeInsignia_omiteDuplicado() {
        when(actividadMspRepository.findById(50L)).thenReturn(Optional.of(actividad));
        // Miembro 1 ya la tiene
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(1L, 100L)).thenReturn(true);
        // Miembro 2 no la tiene
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(2L, 100L)).thenReturn(false);

        gamificacionService.otorgarInsigniaOficial(50L);

        ArgumentCaptor<MiembroInsignia> captor = ArgumentCaptor.forClass(MiembroInsignia.class);
        verify(miembroInsigniaRepository, times(1)).save(captor.capture());

        MiembroInsignia insigniaGuardada = captor.getValue();
        assertThat(insigniaGuardada.getMiembro().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("No realiza inserciones si todos los participantes ya tienen la insignia")
    void otorgarInsigniaOficial_todosPoseenInsignia_noGuardaNada() {
        when(actividadMspRepository.findById(50L)).thenReturn(Optional.of(actividad));
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(1L, 100L)).thenReturn(true);
        when(miembroInsigniaRepository.existsByMiembroIdAndSaberAncestralId(2L, 100L)).thenReturn(true);

        gamificacionService.otorgarInsigniaOficial(50L);

        verify(miembroInsigniaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lanza RecursoNoEncontradoException cuando la actividad no existe")
    void otorgarInsigniaOficial_actividadNoExiste_lanzaExcepcion() {
        when(actividadMspRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gamificacionService.otorgarInsigniaOficial(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("999");

        verify(miembroInsigniaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Omite adjudicación si el saber ancestral no tiene nombre de insignia oficial")
    void otorgarInsigniaOficial_saberSinNombreInsignia_omiteCreacion() {
        saberAncestral.setNombreInsigniaOficial(null);
        when(actividadMspRepository.findById(50L)).thenReturn(Optional.of(actividad));

        gamificacionService.otorgarInsigniaOficial(50L);

        verify(miembroInsigniaRepository, never()).save(any());

        saberAncestral.setNombreInsigniaOficial("   ");
        gamificacionService.otorgarInsigniaOficial(50L);

        verify(miembroInsigniaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Maneja de forma segura actividades sin participantes")
    void otorgarInsigniaOficial_actividadSinParticipantes_noFalla() {
        actividad.setParticipantes(null);
        when(actividadMspRepository.findById(50L)).thenReturn(Optional.of(actividad));

        gamificacionService.otorgarInsigniaOficial(50L);

        verify(miembroInsigniaRepository, never()).save(any());
    }
}

