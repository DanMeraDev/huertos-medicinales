package com.huertos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.CrearSubhuertoRequest;
import com.huertos.dto.SubhuertoResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.Comunidad;
import com.huertos.model.Huerto;
import com.huertos.model.Subhuerto;
import com.huertos.model.SubtipoSubhuerto;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.SubhuertoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubhuertoServiceImplTest {

    @Mock
    private SubhuertoRepository subhuertoRepository;

    @Mock
    private HuertoRepository huertoRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @InjectMocks
    private SubhuertoServiceImpl service;

    private Comunidad comunidad;
    private Huerto huerto;

    @BeforeEach
    void setUp() {
        comunidad = new Comunidad();
        comunidad.setId(5L);
        comunidad.setActiva(true);

        huerto = new Huerto();
        huerto.setId(20L);
        huerto.setComunidad(comunidad);
        huerto.setAreaTotalM2(new BigDecimal("100.00"));
        huerto.setActivo(true);
    }

    @Test
    void crearSubhuerto_persisteConActivoTrueCuandoCumpleReglas() {
        var request = new CrearSubhuertoRequest(
                "  Zona Norte Surcos  ",
                SubtipoSubhuerto.SURCOS_WACHU,
                new BigDecimal("35.50")
        );

        when(comunidadRepository.findByResponsableCorreo("resp@comunidad.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(5L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findByHuertoIdAndActivoTrueOrderByCreadoEnAsc(20L))
                .thenReturn(List.of());
        when(subhuertoRepository.save(any(Subhuerto.class))).thenAnswer(inv -> {
            Subhuerto s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        SubhuertoResponse respuesta = service.crear("resp@comunidad.ec", request);

        assertNotNull(respuesta);
        assertEquals(99L, respuesta.id());
        assertEquals(20L, respuesta.huertoId());
        assertEquals("Zona Norte Surcos", respuesta.nombreZona());
        assertEquals(SubtipoSubhuerto.SURCOS_WACHU, respuesta.subtipo());
        assertEquals(new BigDecimal("35.50"), respuesta.areaM2());
        assertTrue(respuesta.activo());
    }

    @Test
    void crearSubhuerto_lanzaReglaNegocioSiHuertoNoTieneAreaTotal() {
        huerto.setAreaTotalM2(null);
        var request = new CrearSubhuertoRequest("Zona 1", SubtipoSubhuerto.MACETAS_PISO, new BigDecimal("10.00"));

        when(comunidadRepository.findByResponsableCorreo("resp@comunidad.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(5L))
                .thenReturn(Optional.of(huerto));

        ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> service.crear("resp@comunidad.ec", request));

        assertEquals("Primero completa el área del huerto", ex.getMessage());
        verify(subhuertoRepository, never()).save(any());
    }

    @Test
    void crearSubhuerto_lanzaReglaNegocioSiExcedeMaximoTresSubtiposDistintos() {
        Subhuerto s1 = crearExistente(1L, SubtipoSubhuerto.SURCOS_WACHU, "20.00");
        Subhuerto s2 = crearExistente(2L, SubtipoSubhuerto.TERRAZAS_ANTI, "20.00");
        Subhuerto s3 = crearExistente(3L, SubtipoSubhuerto.MACETAS_PISO, "20.00");

        var requestCuartoSubtipo = new CrearSubhuertoRequest(
                "Zona Vertical",
                SubtipoSubhuerto.VERTICAL_PARED,
                new BigDecimal("10.00")
        );

        when(comunidadRepository.findByResponsableCorreo("resp@comunidad.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(5L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findByHuertoIdAndActivoTrueOrderByCreadoEnAsc(20L))
                .thenReturn(List.of(s1, s2, s3));

        ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> service.crear("resp@comunidad.ec", requestCuartoSubtipo));

        assertEquals("Máximo 3 subtipos por huerto según norma MSP", ex.getMessage());
        verify(subhuertoRepository, never()).save(any());
    }

    @Test
    void crearSubhuerto_permiteMismoSubtipoAunqueYaHayaTresSubtiposEnUso() {
        Subhuerto s1 = crearExistente(1L, SubtipoSubhuerto.SURCOS_WACHU, "20.00");
        Subhuerto s2 = crearExistente(2L, SubtipoSubhuerto.TERRAZAS_ANTI, "20.00");
        Subhuerto s3 = crearExistente(3L, SubtipoSubhuerto.MACETAS_PISO, "20.00");

        var requestSubtipoRepetido = new CrearSubhuertoRequest(
                "Surcos Sur",
                SubtipoSubhuerto.SURCOS_WACHU,
                new BigDecimal("15.00")
        );

        when(comunidadRepository.findByResponsableCorreo("resp@comunidad.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(5L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findByHuertoIdAndActivoTrueOrderByCreadoEnAsc(20L))
                .thenReturn(List.of(s1, s2, s3));
        when(subhuertoRepository.save(any(Subhuerto.class))).thenAnswer(inv -> {
            Subhuerto s = inv.getArgument(0);
            s.setId(4L);
            return s;
        });

        SubhuertoResponse respuesta = service.crear("resp@comunidad.ec", requestSubtipoRepetido);

        assertEquals(4L, respuesta.id());
        assertEquals(SubtipoSubhuerto.SURCOS_WACHU, respuesta.subtipo());
    }

    @Test
    void crearSubhuerto_lanzaReglaNegocioSiAreaSuperaEspacioDisponible() {
        Subhuerto s1 = crearExistente(1L, SubtipoSubhuerto.SURCOS_WACHU, "75.00");

        var requestExcedeArea = new CrearSubhuertoRequest(
                "Terrazas Este",
                SubtipoSubhuerto.TERRAZAS_ANTI,
                new BigDecimal("30.00")
        );

        when(comunidadRepository.findByResponsableCorreo("resp@comunidad.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(5L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findByHuertoIdAndActivoTrueOrderByCreadoEnAsc(20L))
                .thenReturn(List.of(s1));

        ReglaNegocioException ex = assertThrows(
                ReglaNegocioException.class,
                () -> service.crear("resp@comunidad.ec", requestExcedeArea));

        assertEquals("El área ingresada supera el espacio disponible del huerto", ex.getMessage());
        verify(subhuertoRepository, never()).save(any());
    }

    @Test
    void listarPorUsuario_lanzaNoEncontradoSiComunidadNoExiste() {
        when(comunidadRepository.findByResponsableCorreo("desconocido@comunidad.ec"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNoEncontradoException.class,
                () -> service.listarPorUsuario("desconocido@comunidad.ec"));
    }

    private Subhuerto crearExistente(Long id, SubtipoSubhuerto subtipo, String area) {
        Subhuerto s = new Subhuerto();
        s.setId(id);
        s.setHuerto(huerto);
        s.setNombreZona("Zona " + id);
        s.setSubtipo(subtipo);
        s.setAreaM2(new BigDecimal(area));
        s.setActivo(true);
        return s;
    }
}