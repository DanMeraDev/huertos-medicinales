package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.config.ConstantesGamificacion;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.model.ActividadComunidad;
import com.huertos.model.CategoriaActividad;
import com.huertos.model.Comunidad;
import com.huertos.model.Huerto;
import com.huertos.model.Miembro;
import com.huertos.model.MiembroInsignia;
import com.huertos.model.NivelMiembro;
import com.huertos.model.RegistroActividadMiembro;
import com.huertos.model.TalentoComunidad;
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
class GamificacionCotidianaTest {

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
    private Miembro voluntario;
    private ActividadComunidad actividadAbonaje;
    private TalentoComunidad talentoAbonaje;
    private RegistroActividadMiembro registro;

    @BeforeEach
    void setUp() {
        comunidad = new Comunidad();
        comunidad.setId(1L);
        comunidad.setNombre("Comunidad San Pedro");
        comunidad.setPuntosComunidad(120);

        huerto = new Huerto();
        huerto.setId(10L);
        huerto.setNombreHuerto("Huerto El Sol");
        huerto.setPuntosHuerto(80);
        huerto.setComunidad(comunidad);

        voluntario = new Miembro();
        voluntario.setId(201L);
        voluntario.setNombres("Luis");
        voluntario.setApellidos("Morales");
        voluntario.setNivel(NivelMiembro.APRENDIZ);
        voluntario.setPuntosTotales(40);
        voluntario.setActivo(true);

        actividadAbonaje = new ActividadComunidad();
        actividadAbonaje.setId(501L);
        actividadAbonaje.setNombre("Abonaje de bancales");
        actividadAbonaje.setCategoria(CategoriaActividad.ABONO_COMPOST);
        actividadAbonaje.setPuntosPorHora(2);
        actividadAbonaje.setActiva(true);
        actividadAbonaje.setComunidad(comunidad);

        talentoAbonaje = new TalentoComunidad();
        talentoAbonaje.setId(701L);
        talentoAbonaje.setComunidad(comunidad);
        talentoAbonaje.setActividadComunidad(actividadAbonaje);
        talentoAbonaje.setNombreInsignia("Maestro del Abono");
        talentoAbonaje.setDescripcion("Experto en técnicas de compostaje y abonaje");
        talentoAbonaje.setMetaRepeticiones(50);
        talentoAbonaje.setIconoCodigo("icono-abono");
        talentoAbonaje.setActivo(true);

        registro = new RegistroActividadMiembro();
        registro.setId(901L);
        registro.setComunidad(comunidad);
        registro.setMiembro(voluntario);
        registro.setActividadComunidad(actividadAbonaje);
        registro.setFechaActividad(LocalDate.now());
        registro.setHorasTrabajadas(3);
        registro.setPuntosOtorgados(0);
    }

    @Test
    @DisplayName("Acredita puntos calculados exclusivamente al miembro sin alterar comunidad ni huerto")
    void debeAcreditarPuntosExclusivamenteAlMiembroSinAlterarComunidadNiHuerto() {
        // Arrange
        when(registroActividadMiembroRepository.findById(901L)).thenReturn(Optional.of(registro));
        when(miembroRepository.findById(201L)).thenReturn(Optional.of(voluntario));
        when(registroActividadMiembroRepository.countByMiembroIdAndActividadComunidadId(201L, 501L)).thenReturn(10L);
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(501L)).thenReturn(List.of(talentoAbonaje));

        // Act
        gamificacionService.procesarActividadCotidiana(901L);

        // Assert: 3 horas * 2 puntos/hora = 6 puntos acreditados
        assertThat(registro.getPuntosOtorgados()).isEqualTo(6);
        assertThat(voluntario.getPuntosTotales()).isEqualTo(46);

        // Verifica que se guardaron el registro y el miembro
        verify(registroActividadMiembroRepository).save(registro);
        verify(miembroRepository).save(voluntario);

        // Verifica que comunidad y huerto nunca se tocaron
        verify(comunidadRepository, never()).save(any());
        verify(huertoRepository, never()).save(any());
        assertThat(comunidad.getPuntosComunidad()).isEqualTo(120);
        assertThat(huerto.getPuntosHuerto()).isEqualTo(80);
    }

    @Test
    @DisplayName("Desbloquea talento comunitario al cumplir exactamente la meta de repeticiones (50)")
    void debeDesbloquearTalentoComunitarioAlCumplirMetaDeRepeticiones() {
        // Arrange
        when(registroActividadMiembroRepository.findById(901L)).thenReturn(Optional.of(registro));
        when(miembroRepository.findById(201L)).thenReturn(Optional.of(voluntario));
        when(registroActividadMiembroRepository.countByMiembroIdAndActividadComunidadId(201L, 501L)).thenReturn(50L);
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(501L)).thenReturn(List.of(talentoAbonaje));
        when(miembroInsigniaRepository.existsByMiembroIdAndTalentoComunidadId(201L, 701L)).thenReturn(false);

        // Act
        gamificacionService.procesarActividadCotidiana(901L);

        // Assert
        ArgumentCaptor<MiembroInsignia> insigniaCaptor = ArgumentCaptor.forClass(MiembroInsignia.class);
        verify(miembroInsigniaRepository).save(insigniaCaptor.capture());

        MiembroInsignia ganada = insigniaCaptor.getValue();
        assertThat(ganada.getMiembro()).isEqualTo(voluntario);
        assertThat(ganada.getComunidad()).isEqualTo(comunidad);
        assertThat(ganada.getTipoOrigen()).isEqualTo(TipoOrigenInsignia.TALENTO_COMUNIDAD);
        assertThat(ganada.getNombreInsignia()).isEqualTo("Maestro del Abono");
        assertThat(ganada.getDescripcion()).isEqualTo("Experto en técnicas de compostaje y abonaje");
        assertThat(ganada.getIconoCodigo()).isEqualTo("icono-abono");
        assertThat(ganada.getTalentoComunidad()).isEqualTo(talentoAbonaje);
        assertThat(ganada.getFechaObtencion()).isEqualTo(registro.getFechaActividad());
    }

    @Test
    @DisplayName("No duplica el talento comunitario en repeticiones posteriores si ya lo posee")
    void noDebeDuplicarTalentoSiElMiembroYaPoseeLaInsignia() {
        // Arrange
        when(registroActividadMiembroRepository.findById(901L)).thenReturn(Optional.of(registro));
        when(miembroRepository.findById(201L)).thenReturn(Optional.of(voluntario));
        when(registroActividadMiembroRepository.countByMiembroIdAndActividadComunidadId(201L, 501L)).thenReturn(51L);
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(501L)).thenReturn(List.of(talentoAbonaje));
        when(miembroInsigniaRepository.existsByMiembroIdAndTalentoComunidadId(201L, 701L)).thenReturn(true);

        // Act
        gamificacionService.procesarActividadCotidiana(901L);

        // Assert
        verify(miembroInsigniaRepository, never()).save(any(MiembroInsignia.class));
    }

    @Test
    @DisplayName("No desbloquea talento comunitario si no alcanza la meta de repeticiones requerida")
    void noDebeDesbloquearTalentoSiNoAlcanzaLaMetaDeRepeticiones() {
        // Arrange
        when(registroActividadMiembroRepository.findById(901L)).thenReturn(Optional.of(registro));
        when(miembroRepository.findById(201L)).thenReturn(Optional.of(voluntario));
        when(registroActividadMiembroRepository.countByMiembroIdAndActividadComunidadId(201L, 501L)).thenReturn(32L);
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(501L)).thenReturn(List.of(talentoAbonaje));

        // Act
        gamificacionService.procesarActividadCotidiana(901L);

        // Assert
        verify(miembroInsigniaRepository, never()).save(any(MiembroInsignia.class));
        assertThat(voluntario.getPuntosTotales()).isEqualTo(46);
    }

    @Test
    @DisplayName("Asciende honoríficamente al miembro de nivel al alcanzar el umbral de puntos (GM-CU02)")
    void debeAscenderNivelAlAlcanzarUmbralDePuntos() {
        // Arrange: Voluntario tiene 95 puntos; con 3 horas * 2 = 6 puntos alcanzará 101 (>= 100 OFICIAL)
        voluntario.setPuntosTotales(95);
        when(registroActividadMiembroRepository.findById(901L)).thenReturn(Optional.of(registro));
        when(miembroRepository.findById(201L)).thenReturn(Optional.of(voluntario));
        when(registroActividadMiembroRepository.countByMiembroIdAndActividadComunidadId(201L, 501L)).thenReturn(5L);
        when(talentoComunidadRepository.findByActividadComunidadIdAndActivoTrue(501L)).thenReturn(List.of());

        // Act
        gamificacionService.procesarActividadCotidiana(901L);

        // Assert
        assertThat(voluntario.getPuntosTotales()).isEqualTo(101);
        assertThat(voluntario.getNivel()).isEqualTo(NivelMiembro.OFICIAL);
        verify(miembroRepository).save(voluntario);
    }

    @Test
    @DisplayName("Lanza RecursoNoEncontradoException cuando el registro de actividad cotidiana no existe")
    void debeLanzarExcepcionCuandoRegistroNoExiste() {
        // Arrange
        when(registroActividadMiembroRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> gamificacionService.procesarActividadCotidiana(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("999");
    }
}
