package com.huertos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.RegistrarSiembraRequest;
import com.huertos.dto.SiembraDetalleResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.SiembraMapper;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.Comunidad;
import com.huertos.model.CualidadTermica;
import com.huertos.model.EstadoCultivo;
import com.huertos.model.FaseLunar;
import com.huertos.model.Huerto;
import com.huertos.model.Planta;
import com.huertos.model.Siembra;
import com.huertos.model.Subhuerto;
import com.huertos.repository.BodegaMaterialVegetalRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.HuertoRepository;
import com.huertos.repository.SiembraRepository;
import com.huertos.repository.SubhuertoRepository;
import com.huertos.service.impl.SiembraServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiembraServiceImplTest {

    @Mock
    private SiembraRepository siembraRepository;

    @Mock
    private SubhuertoRepository subhuertoRepository;

    @Mock
    private BodegaMaterialVegetalRepository bodegaMaterialVegetalRepository;

    @Mock
    private HuertoRepository huertoRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private BodegaService bodegaService;

    @Mock
    private SiembraMapper siembraMapper;

    @InjectMocks
    private SiembraServiceImpl service;

    private Comunidad comunidad;
    private Huerto huerto;
    private Subhuerto subhuerto;
    private BodegaMaterialVegetal bodegaMaterial;
    private Planta planta;

    @BeforeEach
    void setUp() {
        comunidad = new Comunidad();
        comunidad.setId(1L);
        comunidad.setActiva(true);

        huerto = new Huerto();
        huerto.setId(10L);
        huerto.setComunidad(comunidad);
        huerto.setAltitudMsnm(2500);
        huerto.setActivo(true);

        subhuerto = new Subhuerto();
        subhuerto.setId(20L);
        subhuerto.setHuerto(huerto);
        subhuerto.setNombreZona("Zona Hierbas Andinas");
        subhuerto.setActivo(true);

        planta = new Planta();
        planta.setId(100L);
        planta.setNombreComun("Matico");
        planta.setAltitudMinMsnm(2000);
        planta.setAltitudMaxMsnm(3000);
        planta.setCualidadTermica(CualidadTermica.CALIENTE);

        bodegaMaterial = new BodegaMaterialVegetal();
        bodegaMaterial.setId(50L);
        bodegaMaterial.setComunidad(comunidad);
        bodegaMaterial.setPlanta(planta);
        bodegaMaterial.setCantidadDisponible(new BigDecimal("15.00"));
        bodegaMaterial.setActivo(true);
    }

    @Test
    void registrarSiembra_exitoso_sinAlertaAltitud() {
        var request = new RegistrarSiembraRequest(
                20L,
                50L,
                new BigDecimal("5.00"),
                LocalDate.now(),
                FaseLunar.WINAK_KILLA_CRECIENTE,
                false
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(1L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findById(20L))
                .thenReturn(Optional.of(subhuerto));
        when(bodegaMaterialVegetalRepository.findById(50L))
                .thenReturn(Optional.of(bodegaMaterial));

        SiembraDetalleResponse esperado = new SiembraDetalleResponse(
                99L,
                20L,
                "Zona Hierbas Andinas",
                50L,
                100L,
                "Matico",
                CualidadTermica.CALIENTE,
                new BigDecimal("5.00"),
                request.fechaSiembra(),
                FaseLunar.WINAK_KILLA_CRECIENTE,
                false,
                EstadoCultivo.SEMBRADO
        );

        when(siembraRepository.save(any(Siembra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(siembraMapper.toSiembraDetalleResponse(any(Siembra.class))).thenReturn(esperado);

        SiembraDetalleResponse resultado = service.registrarSiembra("resp@huertos.gob.ec", request);

        assertNotNull(resultado);
        assertEquals(esperado.id(), resultado.id());
        assertFalse(resultado.tuvoAlertaAltitud());
        assertEquals(EstadoCultivo.SEMBRADO, resultado.estadoCultivo());
        assertEquals(CualidadTermica.CALIENTE, resultado.cualidadTermica());

        verify(bodegaService).descontarStockPorSiembra(50L, new BigDecimal("5.00"));

        ArgumentCaptor<Siembra> captor = ArgumentCaptor.forClass(Siembra.class);
        verify(siembraRepository).save(captor.capture());
        Siembra guardada = captor.getValue();
        assertFalse(guardada.isTuvoAlertaAltitud());
        assertEquals(EstadoCultivo.SEMBRADO, guardada.getEstadoCultivo());
    }

    @Test
    void registrarSiembra_falla_discrepanciaAltitudSinAceptarRiesgo() {
        huerto.setAltitudMsnm(3500);

        var request = new RegistrarSiembraRequest(
                20L,
                50L,
                new BigDecimal("3.00"),
                LocalDate.now(),
                FaseLunar.PURA_KILLA_LLENA,
                false
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(1L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findById(20L))
                .thenReturn(Optional.of(subhuerto));
        when(bodegaMaterialVegetalRepository.findById(50L))
                .thenReturn(Optional.of(bodegaMaterial));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.registrarSiembra("resp@huertos.gob.ec", request));

        assertTrue(ex.getMessage().toLowerCase().contains("altitud"));
        verify(bodegaService, never()).descontarStockPorSiembra(any(), any());
        verify(siembraRepository, never()).save(any());
    }

    @Test
    void registrarSiembra_exitoso_conAlertaAltitudAceptada() {
        huerto.setAltitudMsnm(3500);

        var request = new RegistrarSiembraRequest(
                20L,
                50L,
                new BigDecimal("3.00"),
                LocalDate.now(),
                FaseLunar.PURA_KILLA_LLENA,
                true
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(1L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findById(20L))
                .thenReturn(Optional.of(subhuerto));
        when(bodegaMaterialVegetalRepository.findById(50L))
                .thenReturn(Optional.of(bodegaMaterial));

        when(siembraRepository.save(any(Siembra.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarSiembra("resp@huertos.gob.ec", request);

        verify(bodegaService).descontarStockPorSiembra(50L, new BigDecimal("3.00"));

        ArgumentCaptor<Siembra> captor = ArgumentCaptor.forClass(Siembra.class);
        verify(siembraRepository).save(captor.capture());
        Siembra guardada = captor.getValue();
        assertTrue(guardada.isTuvoAlertaAltitud());
        assertEquals(EstadoCultivo.SEMBRADO, guardada.getEstadoCultivo());
    }

    @Test
    void registrarSiembra_falla_stockInsuficiente() {
        bodegaMaterial.setCantidadDisponible(new BigDecimal("2.00"));

        var request = new RegistrarSiembraRequest(
                20L,
                50L,
                new BigDecimal("5.00"),
                LocalDate.now(),
                FaseLunar.YANA_KILLA_OSCURA,
                false
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(1L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findById(20L))
                .thenReturn(Optional.of(subhuerto));
        when(bodegaMaterialVegetalRepository.findById(50L))
                .thenReturn(Optional.of(bodegaMaterial));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.registrarSiembra("resp@huertos.gob.ec", request));

        assertEquals("Stock insuficiente en bodega", ex.getMessage());
        verify(bodegaService, never()).descontarStockPorSiembra(any(), any());
        verify(siembraRepository, never()).save(any());
    }

    @Test
    void registrarSiembra_falla_subhuertoPerteneceAOtroHuerto() {
        Huerto otroHuerto = new Huerto();
        otroHuerto.setId(999L);
        subhuerto.setHuerto(otroHuerto);

        var request = new RegistrarSiembraRequest(
                20L,
                50L,
                new BigDecimal("1.00"),
                LocalDate.now(),
                FaseLunar.WANUK_KILLA_MENGUANTE,
                false
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(1L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findById(20L))
                .thenReturn(Optional.of(subhuerto));

        assertThrows(ReglaNegocioException.class,
                () -> service.registrarSiembra("resp@huertos.gob.ec", request));

        verify(siembraRepository, never()).save(any());
    }

    @Test
    void registrarSiembra_falla_lotePerteneceAOtraComunidad() {
        Comunidad otraComunidad = new Comunidad();
        otraComunidad.setId(888L);
        bodegaMaterial.setComunidad(otraComunidad);

        var request = new RegistrarSiembraRequest(
                20L,
                50L,
                new BigDecimal("1.00"),
                LocalDate.now(),
                FaseLunar.WINAK_KILLA_CRECIENTE,
                false
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.of(comunidad));
        when(huertoRepository.findByComunidadId(1L))
                .thenReturn(Optional.of(huerto));
        when(subhuertoRepository.findById(20L))
                .thenReturn(Optional.of(subhuerto));
        when(bodegaMaterialVegetalRepository.findById(50L))
                .thenReturn(Optional.of(bodegaMaterial));

        assertThrows(ReglaNegocioException.class,
                () -> service.registrarSiembra("resp@huertos.gob.ec", request));

        verify(siembraRepository, never()).save(any());
    }

    @Test
    void registrarSiembra_falla_comunidadNoExiste() {
        var request = new RegistrarSiembraRequest(
                20L,
                50L,
                new BigDecimal("1.00"),
                LocalDate.now(),
                FaseLunar.WINAK_KILLA_CRECIENTE,
                false
        );

        when(comunidadRepository.findByResponsableCorreo("resp@huertos.gob.ec"))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.registrarSiembra("resp@huertos.gob.ec", request));
    }
}

