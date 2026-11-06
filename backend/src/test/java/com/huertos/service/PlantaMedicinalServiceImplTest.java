package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.PlantaMedicinalMapper;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.PlantaMedicinal;
import com.huertos.repository.PlantaMedicinalRepository;
import com.huertos.service.impl.PlantaMedicinalServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class PlantaMedicinalServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-11-06T15:30:00Z");
    private static final String MENSAJE_FOTO = "Adjunta una fotografía JPG o PNG de máximo 2 MB";
    private static final String FOTO_URL = "/api/plantas/fotos/3f2b9c1e-0d4a-4c8e-9a71-5b6d2e8f1a00.jpg";
    private static final int DOS_MB = 2 * 1024 * 1024;

    @Mock
    private PlantaMedicinalRepository plantaMedicinalRepository;

    @Mock
    private AlmacenamientoFotoService almacenamientoFotoService;

    private PlantaMedicinalService servicio;

    @BeforeEach
    void configurar() {
        servicio = new PlantaMedicinalServiceImpl(
                plantaMedicinalRepository,
                Mappers.getMapper(PlantaMedicinalMapper.class),
                almacenamientoFotoService,
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void registraLaPlantaConLaFechaDelRelojYLaFotoGuardada() {
        MultipartFile foto = fotoJpg();
        when(almacenamientoFotoService.guardar(foto)).thenReturn(FOTO_URL);
        when(plantaMedicinalRepository.save(any(PlantaMedicinal.class))).thenAnswer(invocacion -> {
            PlantaMedicinal planta = invocacion.getArgument(0);
            planta.setId(1L);
            return planta;
        });

        PlantaResponse respuesta = servicio.registrar(solicitud("Matricaria chamomilla"), foto);

        ArgumentCaptor<PlantaMedicinal> guardada = ArgumentCaptor.forClass(PlantaMedicinal.class);
        verify(plantaMedicinalRepository).save(guardada.capture());
        assertThat(guardada.getValue().getCreadoEn()).isEqualTo(LocalDateTime.ofInstant(AHORA, ZoneOffset.UTC));
        assertThat(guardada.getValue().getFotoUrl()).isEqualTo(FOTO_URL);
        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.nombreComun()).isEqualTo("Manzanilla");
        assertThat(respuesta.nombreCientifico()).isEqualTo("Matricaria chamomilla");
        assertThat(respuesta.cualidadTermica()).isEqualTo(CualidadTermica.CALIENTE);
        assertThat(respuesta.cicloCultivo()).isEqualTo(CicloCultivo.CORTO);
        assertThat(respuesta.fotoUrl()).isEqualTo(FOTO_URL);
    }

    @Test
    void rechazaUnNombreCientificoYaRegistrado() {
        when(plantaMedicinalRepository.existsByNombreCientificoIgnoreCase("Matricaria chamomilla")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(solicitud("Matricaria chamomilla"), fotoJpg()))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe una planta registrada con ese nombre científico");
        verify(plantaMedicinalRepository, never()).save(any());
    }

    @Test
    void rechazaElDuplicadoConDistintasMayusculasYEspacios() {
        when(plantaMedicinalRepository.existsByNombreCientificoIgnoreCase("MATRICARIA chamomilla")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(solicitud("  MATRICARIA chamomilla  "), fotoJpg()))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe una planta registrada con ese nombre científico");
        verify(plantaMedicinalRepository, never()).save(any());
    }

    @Test
    void rechazaLaPlantaSinFoto() {
        assertFotoRechazada(null);
    }

    @Test
    void rechazaUnaFotoVacia() {
        assertFotoRechazada(new MockMultipartFile("foto", "manzanilla.jpg", "image/jpeg", new byte[0]));
    }

    @Test
    void rechazaUnaFotoQueNoEsJpgNiPng() {
        assertFotoRechazada(new MockMultipartFile("foto", "manzanilla.gif", "image/gif", new byte[] {1, 2, 3}));
    }

    @Test
    void rechazaUnaFotoDeMasDeDosMegas() {
        assertFotoRechazada(new MockMultipartFile("foto", "manzanilla.jpg", "image/jpeg", new byte[DOS_MB + 1]));
    }

    @Test
    void aceptaUnaFotoPngDeExactamenteDosMegas() {
        MultipartFile foto = new MockMultipartFile("foto", "manzanilla.png", "image/png", new byte[DOS_MB]);
        when(almacenamientoFotoService.guardar(foto)).thenReturn(FOTO_URL);
        when(plantaMedicinalRepository.save(any(PlantaMedicinal.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        PlantaResponse respuesta = servicio.registrar(solicitud("Matricaria chamomilla"), foto);

        assertThat(respuesta.fotoUrl()).isEqualTo(FOTO_URL);
    }

    private void assertFotoRechazada(MultipartFile foto) {
        assertThatThrownBy(() -> servicio.registrar(solicitud("Matricaria chamomilla"), foto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage(MENSAJE_FOTO);
        verify(almacenamientoFotoService, never()).guardar(any());
        verify(plantaMedicinalRepository, never()).save(any());
    }

    private static MultipartFile fotoJpg() {
        return new MockMultipartFile("foto", "manzanilla.jpg", "image/jpeg", new byte[] {1, 2, 3});
    }

    private static CrearPlantaRequest solicitud(String nombreCientifico) {
        return new CrearPlantaRequest(
                "Manzanilla",
                nombreCientifico,
                "Manzanilla panga",
                "Asteraceae",
                "Sierra: Pichincha, Imbabura",
                "2000 a 3000",
                CicloCultivo.CORTO,
                CualidadTermica.CALIENTE,
                "Digestivo, calmante",
                "Una taza de infusión tres veces al día",
                "No usar en embarazo");
    }
}
