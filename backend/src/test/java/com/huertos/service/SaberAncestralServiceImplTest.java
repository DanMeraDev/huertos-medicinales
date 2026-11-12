package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.huertos.dto.SaberAncestralDetalleResponse;
import com.huertos.dto.SaberAncestralResumenResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.SaberAncestralMapper;
import com.huertos.model.CategoriaSaber;
import com.huertos.model.FaseLunar;
import com.huertos.model.SaberAncestral;
import com.huertos.repository.SaberAncestralRepository;
import com.huertos.service.impl.SaberAncestralServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaberAncestralServiceImplTest {

    @Mock
    private SaberAncestralRepository saberAncestralRepository;

    private SaberAncestralService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new SaberAncestralServiceImpl(
                saberAncestralRepository, Mappers.getMapper(SaberAncestralMapper.class));
    }

    @Test
    void listaLaRutaDeSaberesEnOrdenSecuencialDePasos() {
        when(saberAncestralRepository.findAllByOrderByOrdenPasoAsc())
                .thenReturn(List.of(saber(1L, 1), saber(2L, 2), saber(3L, 3)));

        List<SaberAncestralResumenResponse> ruta = servicio.listarRutaSaberes();

        assertThat(ruta).extracting(SaberAncestralResumenResponse::ordenPaso)
                .containsExactly(1, 2, 3);
        assertThat(ruta).extracting(SaberAncestralResumenResponse::codigoHito)
                .containsExactly("SA-PASO-1", "SA-PASO-2", "SA-PASO-3");
    }

    @Test
    void devuelveElDetalleDelSaberSolicitado() {
        SaberAncestral saber = saber(2L, 2);
        when(saberAncestralRepository.findById(2L)).thenReturn(Optional.of(saber));

        SaberAncestralDetalleResponse detalle = servicio.obtenerPorId(2L);

        assertThat(detalle.id()).isEqualTo(2L);
        assertThat(detalle.ordenPaso()).isEqualTo(2);
        assertThat(detalle.titulo()).isEqualTo("Preparación de la tierra");
        assertThat(detalle.tituloKichwa()).isEqualTo("Allpa wiñay");
        assertThat(detalle.horarioEnergetico()).isEqualTo("05:00 a 07:00 AM - Salida del sol");
        assertThat(detalle.puntosBaseComunidad()).isEqualTo(20);
    }

    @Test
    void avisaCuandoElSaberSolicitadoNoExiste() {
        when(saberAncestralRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("El saber ancestral solicitado no fue encontrado");
    }

    private SaberAncestral saber(Long id, int ordenPaso) {
        SaberAncestral saber = new SaberAncestral();
        saber.setId(id);
        saber.setOrdenPaso(ordenPaso);
        saber.setCodigoHito("SA-PASO-" + ordenPaso);
        saber.setTitulo("Preparación de la tierra");
        saber.setTituloKichwa("Allpa wiñay");
        saber.setCategoria(CategoriaSaber.PREPARACION_TIERRA);
        saber.setFaseLunarIdeal(FaseLunar.YANA_KILLA_OSCURA);
        saber.setHorarioEnergetico("05:00 a 07:00 AM - Salida del sol");
        saber.setContenidoEducativo("Contenido pedagógico del paso " + ordenPaso);
        saber.setIngredientesHerramientas("Chakana, chaquetaclla");
        saber.setPuntosBaseComunidad(20);
        saber.setNombreInsigniaOficial("Guardián de la tierra");
        return saber;
    }
}
