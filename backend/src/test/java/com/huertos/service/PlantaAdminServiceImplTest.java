package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.PlantaMapper;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import com.huertos.model.Planta;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.impl.PlantaAdminServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlantaAdminServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-11-12T15:30:00Z");
    private static final LocalDateTime FECHA_DEL_RELOJ = LocalDateTime.ofInstant(AHORA, ZoneOffset.UTC);

    @Mock
    private PlantaRepository plantaRepository;

    @Captor
    private ArgumentCaptor<Planta> plantaGuardada;

    private PlantaAdminService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new PlantaAdminServiceImpl(plantaRepository, Mappers.getMapper(PlantaMapper.class),
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void registraLaPlantaActivaConLasFechasDelReloj() {
        when(plantaRepository.existsByNombreCientificoIgnoreCase("Herba ficticia")).thenReturn(false);
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));

        servicio.registrarPlanta(solicitudCompleta());

        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.isActiva()).isTrue();
        assertThat(planta.getCreadoEn()).isEqualTo(FECHA_DEL_RELOJ);
        assertThat(planta.getActualizadoEn()).isEqualTo(FECHA_DEL_RELOJ);
        assertThat(planta.getId()).isEqualTo(50L);
    }

    @Test
    void devuelveLaFichaDeLaPlantaRegistrada() {
        when(plantaRepository.existsByNombreCientificoIgnoreCase("Herba ficticia")).thenReturn(false);
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));

        PlantaDetalleResponse ficha = servicio.registrarPlanta(solicitudCompleta());

        assertThat(ficha).isEqualTo(new PlantaDetalleResponse(50L, "Hierba de prueba", "Yura kiwa",
                "Herba ficticia", "Ficticiaceae", CualidadTermica.FRIA, CicloCultivo.CORTO, OrigenPlanta.NATIVA,
                2000, 3200, "Costa, Sierra", "Alivia el dolor de estómago", "Infusión de 5 g en una taza",
                "No usar en embarazo", Set.of(CategoriaUso.SEDANTE, CategoriaUso.DIURETICA),
                "plantas/0b7c6e8a-1d2f-4a3b-9c4d-5e6f7a8b9c0d.jpg"));
    }

    @Test
    void guardaTodasLasCategoriasDeLaPlanta() {
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));

        servicio.registrarPlanta(solicitudCompleta());

        verify(plantaRepository).save(plantaGuardada.capture());
        assertThat(plantaGuardada.getValue().getCategorias())
                .containsExactlyInAnyOrder(CategoriaUso.SEDANTE, CategoriaUso.DIURETICA);
    }

    @Test
    void recortaLosEspaciosDeLosTextos() {
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));
        CrearPlantaRequest solicitud = new CrearPlantaRequest("  Hierba de prueba ", " Yura kiwa ",
                "  Herba ficticia  ", " Ficticiaceae ", CualidadTermica.FRIA, null, null, null, null,
                " Costa, Sierra ", "  Alivia el dolor de estómago ", " Infusión ", " No usar en embarazo ",
                Set.of(CategoriaUso.SEDANTE), " plantas/foto.jpg ");

        servicio.registrarPlanta(solicitud);

        verify(plantaRepository).existsByNombreCientificoIgnoreCase("Herba ficticia");
        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.getNombreComun()).isEqualTo("Hierba de prueba");
        assertThat(planta.getNombreKichwa()).isEqualTo("Yura kiwa");
        assertThat(planta.getNombreCientifico()).isEqualTo("Herba ficticia");
        assertThat(planta.getFamiliaBotanica()).isEqualTo("Ficticiaceae");
        assertThat(planta.getRegionesDistribucion()).isEqualTo("Costa, Sierra");
        assertThat(planta.getUsosMedicinales()).isEqualTo("Alivia el dolor de estómago");
        assertThat(planta.getPreparacionDosificacion()).isEqualTo("Infusión");
        assertThat(planta.getContraindicaciones()).isEqualTo("No usar en embarazo");
        assertThat(planta.getFotografiaUrl()).isEqualTo("plantas/foto.jpg");
    }

    @Test
    void guardaComoNuloLosTextosOpcionalesVacios() {
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));
        CrearPlantaRequest solicitud = new CrearPlantaRequest("Hierba de prueba", "   ", "Herba ficticia",
                "Ficticiaceae", CualidadTermica.CALIENTE, null, null, null, null, "", "Alivia el dolor", " ",
                "", Set.of(CategoriaUso.SEDANTE), "  ");

        servicio.registrarPlanta(solicitud);

        verify(plantaRepository).save(plantaGuardada.capture());
        Planta planta = plantaGuardada.getValue();
        assertThat(planta.getNombreKichwa()).isNull();
        assertThat(planta.getRegionesDistribucion()).isNull();
        assertThat(planta.getPreparacionDosificacion()).isNull();
        assertThat(planta.getContraindicaciones()).isNull();
        assertThat(planta.getFotografiaUrl()).isNull();
    }

    @Test
    void rechazaUnNombreCientificoQueYaExisteSinGuardar() {
        when(plantaRepository.existsByNombreCientificoIgnoreCase("Herba ficticia")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarPlanta(solicitudCompleta()))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("El nombre científico ya existe en el catálogo");
        verify(plantaRepository, never()).save(any());
    }

    @Test
    void rechazaUnNombreCientificoDuplicadoEscritoConOtrasMayusculas() {
        CrearPlantaRequest solicitud = new CrearPlantaRequest("Hierba de prueba", null, "HERBA FICTICIA",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, null, null, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null);
        when(plantaRepository.existsByNombreCientificoIgnoreCase("HERBA FICTICIA")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrarPlanta(solicitud))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("El nombre científico ya existe en el catálogo");
        verify(plantaRepository, never()).save(any());
    }

    @Test
    void rechazaUnaAltitudMinimaMayorQueLaMaximaSinConsultar() {
        CrearPlantaRequest solicitud = new CrearPlantaRequest("Hierba de prueba", null, "Herba ficticia",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, 3500, 2000, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null);

        assertThatThrownBy(() -> servicio.registrarPlanta(solicitud))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La altitud mínima no puede ser mayor que la altitud máxima");
        verifyNoInteractions(plantaRepository);
    }

    @Test
    void aceptaAltitudesIgualesYUnaSolaAltitud() {
        when(plantaRepository.save(any(Planta.class))).thenAnswer(invocacion -> conId(invocacion.getArgument(0)));

        servicio.registrarPlanta(new CrearPlantaRequest("Hierba de prueba", null, "Herba ficticia",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, 2500, 2500, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null));
        servicio.registrarPlanta(new CrearPlantaRequest("Hierba de prueba", null, "Herba ficticia dos",
                "Ficticiaceae", CualidadTermica.FRIA, null, null, 3500, null, null, "Alivia el dolor", null, null,
                Set.of(CategoriaUso.SEDANTE), null));

        verify(plantaRepository, times(2)).save(any(Planta.class));
    }

    private static CrearPlantaRequest solicitudCompleta() {
        return new CrearPlantaRequest("Hierba de prueba", "Yura kiwa", "Herba ficticia", "Ficticiaceae",
                CualidadTermica.FRIA, CicloCultivo.CORTO, OrigenPlanta.NATIVA, 2000, 3200, "Costa, Sierra",
                "Alivia el dolor de estómago", "Infusión de 5 g en una taza", "No usar en embarazo",
                Set.of(CategoriaUso.SEDANTE, CategoriaUso.DIURETICA),
                "plantas/0b7c6e8a-1d2f-4a3b-9c4d-5e6f7a8b9c0d.jpg");
    }

    private static Planta conId(Planta planta) {
        planta.setId(50L);
        return planta;
    }
}
