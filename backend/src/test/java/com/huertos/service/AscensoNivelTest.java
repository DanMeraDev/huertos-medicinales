package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.model.Miembro;
import com.huertos.model.NivelMiembro;
import com.huertos.repository.MiembroRepository;
import com.huertos.service.impl.GamificacionServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AscensoNivelTest {

    @Mock
    private MiembroRepository miembroRepository;

    @InjectMocks
    private GamificacionServiceImpl gamificacionService;

    private Miembro miembro;

    @BeforeEach
    void setUp() {
        miembro = new Miembro();
        miembro.setId(1L);
        miembro.setNombres("Rosa");
        miembro.setApellidos("Quishpe");
        miembro.setActivo(true);
    }

    @Test
    @DisplayName("Miembro con 49 puntos permanece en APRENDIZ y no se persiste")
    void miembroCon49Puntos_permaneceEnAprendiz() {
        miembro.setPuntosTotales(49);
        miembro.setNivel(NivelMiembro.APRENDIZ);
        when(miembroRepository.findById(1L)).thenReturn(Optional.of(miembro));

        NivelMiembro nuevoNivel = gamificacionService.evaluarAscensoNivel(1L);

        assertThat(nuevoNivel).isEqualTo(NivelMiembro.APRENDIZ);
        assertThat(miembro.getNivel()).isEqualTo(NivelMiembro.APRENDIZ);
        verify(miembroRepository, never()).save(any(Miembro.class));
    }

    @Test
    @DisplayName("Miembro que llega a 50 puntos asciende a OFICIAL y se persiste el cambio")
    void miembroQueLlegaA50Puntos_asciendeAOficial() {
        miembro.setPuntosTotales(50);
        miembro.setNivel(NivelMiembro.APRENDIZ);
        when(miembroRepository.findById(1L)).thenReturn(Optional.of(miembro));

        NivelMiembro nuevoNivel = gamificacionService.evaluarAscensoNivel(1L);

        assertThat(nuevoNivel).isEqualTo(NivelMiembro.OFICIAL);
        assertThat(miembro.getNivel()).isEqualTo(NivelMiembro.OFICIAL);
        verify(miembroRepository).save(miembro);
    }

    @Test
    @DisplayName("Miembro que llega a 150 puntos asciende a MAESTRO y se persiste el cambio")
    void miembroQueLlegaA150Puntos_asciendeAMaestro() {
        miembro.setPuntosTotales(150);
        miembro.setNivel(NivelMiembro.OFICIAL);
        when(miembroRepository.findById(1L)).thenReturn(Optional.of(miembro));

        NivelMiembro nuevoNivel = gamificacionService.evaluarAscensoNivel(1L);

        assertThat(nuevoNivel).isEqualTo(NivelMiembro.MAESTRO);
        assertThat(miembro.getNivel()).isEqualTo(NivelMiembro.MAESTRO);
        verify(miembroRepository).save(miembro);
    }

    @Test
    @DisplayName("Miembro con 200 puntos que ya es MAESTRO se mantiene en MAESTRO sin actualizar repositorio")
    void miembroCon200PuntosQueYaEsMaestro_seMantieneEnMaestro() {
        miembro.setPuntosTotales(200);
        miembro.setNivel(NivelMiembro.MAESTRO);
        when(miembroRepository.findById(1L)).thenReturn(Optional.of(miembro));

        NivelMiembro nuevoNivel = gamificacionService.evaluarAscensoNivel(1L);

        assertThat(nuevoNivel).isEqualTo(NivelMiembro.MAESTRO);
        assertThat(miembro.getNivel()).isEqualTo(NivelMiembro.MAESTRO);
        verify(miembroRepository, never()).save(any(Miembro.class));
    }

    @Test
    @DisplayName("Lanza RecursoNoEncontradoException cuando el miembro no existe")
    void evaluarAscensoNivel_miembroNoEncontrado_lanzaExcepcion() {
        when(miembroRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gamificacionService.evaluarAscensoNivel(999L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("999");

        verify(miembroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Miembro sin nivel inicial definido adquiere APRENDIZ si tiene menos de 50 puntos")
    void miembroSinNivel_adquiereAprendiz() {
        miembro.setPuntosTotales(20);
        miembro.setNivel(null);
        when(miembroRepository.findById(1L)).thenReturn(Optional.of(miembro));

        NivelMiembro nuevoNivel = gamificacionService.evaluarAscensoNivel(1L);

        assertThat(nuevoNivel).isEqualTo(NivelMiembro.APRENDIZ);
        assertThat(miembro.getNivel()).isEqualTo(NivelMiembro.APRENDIZ);
        verify(miembroRepository).save(miembro);
    }
}
