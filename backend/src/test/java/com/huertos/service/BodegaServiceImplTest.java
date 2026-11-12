package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.BajaMaterialResponse;
import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarBajaMaterialRequest;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.BodegaMapper;
import com.huertos.model.BajaMaterialVegetal;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.CausaFracaso;
import com.huertos.model.Comunidad;
import com.huertos.model.Planta;
import com.huertos.model.TipoMaterialVegetal;
import com.huertos.model.UnidadMedida;
import com.huertos.repository.BajaMaterialVegetalRepository;
import com.huertos.repository.BodegaMaterialVegetalRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.impl.BodegaServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class BodegaServiceImplTest {

    @Mock
    private BodegaMaterialVegetalRepository bodegaRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private PlantaRepository plantaRepository;

    @Mock
    private BajaMaterialVegetalRepository bajaMaterialVegetalRepository;

    @Mock
    private BodegaMapper bodegaMapper;

    @InjectMocks
    private BodegaServiceImpl bodegaService;

    private Comunidad comunidad;
    private Planta planta;
    private RegistrarMaterialVegetalRequest request;

    @BeforeEach
    void setUp() {
        comunidad = new Comunidad();
        comunidad.setId(1L);
        comunidad.setNombre("Comunidad San Pedro");

        planta = new Planta();
        planta.setId(10L);
        planta.setNombreComun("Manzanilla");
        planta.setNombreCientifico("Matricaria chamomilla");
        planta.setActiva(true);

        request = new RegistrarMaterialVegetalRequest(
                10L,
                TipoMaterialVegetal.SEMILLAS,
                new BigDecimal("50.00"),
                UnidadMedida.GRAMOS,
                "Donación vivero local",
                LocalDate.now()
        );
    }

    @Test
    void registrarIngresoMaterialExitoso() {
        BodegaMaterialVegetal entidadGuardada = new BodegaMaterialVegetal();
        entidadGuardada.setId(100L);
        entidadGuardada.setComunidad(comunidad);
        entidadGuardada.setPlanta(planta);
        entidadGuardada.setTipoMaterial(request.tipoMaterial());
        entidadGuardada.setCantidadDisponible(request.cantidad());
        entidadGuardada.setUnidadMedida(request.unidadMedida());
        entidadGuardada.setLoteOrigen(request.loteOrigen());
        entidadGuardada.setFechaIngreso(request.fechaIngreso());

        MaterialVegetalResponse respuestaEsperada = new MaterialVegetalResponse(
                100L,
                1L,
                10L,
                "Manzanilla",
                "Matricaria chamomilla",
                TipoMaterialVegetal.SEMILLAS,
                new BigDecimal("50.00"),
                UnidadMedida.GRAMOS,
                "Donación vivero local",
                request.fechaIngreso()
        );

        when(comunidadRepository.findById(1L)).thenReturn(Optional.of(comunidad));
        when(plantaRepository.findByIdAndActivaTrue(10L)).thenReturn(Optional.of(planta));
        when(bodegaRepository.save(any(BodegaMaterialVegetal.class))).thenReturn(entidadGuardada);
        when(bodegaMapper.toMaterialVegetalResponse(entidadGuardada)).thenReturn(respuestaEsperada);

        MaterialVegetalResponse resultado = bodegaService.registrarIngresoMaterial(1L, request);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(100L);
        assertThat(resultado.comunidadId()).isEqualTo(1L);
        assertThat(resultado.plantaId()).isEqualTo(10L);
        assertThat(resultado.nombreComunPlanta()).isEqualTo("Manzanilla");
        assertThat(resultado.cantidadDisponible()).isEqualTo(new BigDecimal("50.00"));
        assertThat(resultado.unidadMedida()).isEqualTo(UnidadMedida.GRAMOS);

        verify(bodegaRepository).save(any(BodegaMaterialVegetal.class));
    }

    @Test
    void registrarIngresoMaterialLanzaExcepcionCuandoPlantaNoExiste() {
        when(comunidadRepository.findById(1L)).thenReturn(Optional.of(comunidad));
        when(plantaRepository.findByIdAndActivaTrue(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bodegaService.registrarIngresoMaterial(1L, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La planta seleccionada no existe en el catálogo");
    }

    @Test
    void registrarIngresoMaterialLanzaExcepcionCuandoComunidadNoExiste() {
        when(comunidadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bodegaService.registrarIngresoMaterial(99L, request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La comunidad indicada no existe");
    }

    @Test
    void obtenerComunidadIdPorCorreoExitoso() {
        when(comunidadRepository.findByResponsableCorreo("responsable@correo.com")).thenReturn(Optional.of(comunidad));

        Long comunidadId = bodegaService.obtenerComunidadIdPorCorreo("responsable@correo.com");

        assertThat(comunidadId).isEqualTo(1L);
    }

    @Test
    void obtenerComunidadIdPorCorreoLanzaExcepcionSiNoExiste() {
        when(comunidadRepository.findByResponsableCorreo("ajeno@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bodegaService.obtenerComunidadIdPorCorreo("ajeno@correo.com"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe una comunidad asociada al usuario");
    }

    @Test
    void listarMaterialPorComunidadRetornaListaMapeada() {
        BodegaMaterialVegetal entidad = new BodegaMaterialVegetal();
        entidad.setId(1L);
        entidad.setComunidad(comunidad);
        entidad.setPlanta(planta);
        entidad.setTipoMaterial(TipoMaterialVegetal.SEMILLAS);
        entidad.setCantidadDisponible(new BigDecimal("10.00"));
        entidad.setUnidadMedida(UnidadMedida.GRAMOS);

        MaterialVegetalResponse respuesta = new MaterialVegetalResponse(
                1L,
                1L,
                10L,
                "Manzanilla",
                "Matricaria chamomilla",
                TipoMaterialVegetal.SEMILLAS,
                new BigDecimal("10.00"),
                UnidadMedida.GRAMOS,
                "Lote-1",
                LocalDate.of(2026, 11, 10)
        );

        when(bodegaRepository.findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(1L))
                .thenReturn(List.of(entidad));
        when(bodegaMapper.toListMaterialVegetalResponse(List.of(entidad)))
                .thenReturn(List.of(respuesta));

        List<MaterialVegetalResponse> resultado = bodegaService.listarMaterialPorComunidad(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().nombreComunPlanta()).isEqualTo("Manzanilla");
        verify(bodegaRepository).findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(1L);
    }

    @Test
    void listarMaterialPorComunidadRetornaListaVaciaCuandoNoHayRegistros() {
        when(bodegaRepository.findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(1L))
                .thenReturn(List.of());
        when(bodegaMapper.toListMaterialVegetalResponse(List.of()))
                .thenReturn(List.of());

        List<MaterialVegetalResponse> resultado = bodegaService.listarMaterialPorComunidad(1L);

        assertThat(resultado).isEmpty();
    }

    @Test
    void validarAccesoComunidadPermiteAccesoATecnicoMsp() {
        assertThatCode(() -> bodegaService.validarAccesoComunidad(99L, "tecnico@msp.gob.ec", true))
                .doesNotThrowAnyException();
    }

    @Test
    void validarAccesoComunidadPermiteAccesoAResponsableDeSuComunidad() {
        when(comunidadRepository.findByResponsableCorreo("responsable@correo.com")).thenReturn(Optional.of(comunidad));

        assertThatCode(() -> bodegaService.validarAccesoComunidad(1L, "responsable@correo.com", false))
                .doesNotThrowAnyException();
    }

    @Test
    void validarAccesoComunidadLanzaExcepcionParaResponsableDeOtraComunidad() {
        when(comunidadRepository.findByResponsableCorreo("responsable@correo.com")).thenReturn(Optional.of(comunidad));

        assertThatThrownBy(() -> bodegaService.validarAccesoComunidad(2L, "responsable@correo.com", false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No tiene permisos para consultar el inventario de otra comunidad");
    }

    @Test
    void validarAccesoComunidadConAuthenticationTecnicoPermiteAcceso() {
        Authentication auth = mock(Authentication.class);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_TECNICO_MSP"))).when(auth).getAuthorities();

        assertThatCode(() -> bodegaService.validarAccesoComunidad(99L, auth))
                .doesNotThrowAnyException();
    }

    @Test
    void validarAccesoComunidadConAuthenticationResponsableValidaComunidad() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("responsable@correo.com");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_RESPONSABLE_COMUNIDAD"))).when(auth).getAuthorities();
        when(comunidadRepository.findByResponsableCorreo("responsable@correo.com")).thenReturn(Optional.of(comunidad));

        assertThatThrownBy(() -> bodegaService.validarAccesoComunidad(2L, auth))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No tiene permisos para consultar el inventario de otra comunidad");
    }

    @Test
    void validarAccesoComunidadConAuthenticationNullLanzaExcepcion() {
        assertThatThrownBy(() -> bodegaService.validarAccesoComunidad(1L, (Authentication) null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("No autenticado");
    }

    @Test
    void registrarBajaMaterialExitoso() {
        BodegaMaterialVegetal material = new BodegaMaterialVegetal();
        material.setId(10L);
        material.setComunidad(comunidad);
        material.setPlanta(planta);
        material.setTipoMaterial(TipoMaterialVegetal.SEMILLAS);
        material.setCantidadDisponible(new BigDecimal("50.00"));
        material.setUnidadMedida(UnidadMedida.GRAMOS);

        RegistrarBajaMaterialRequest bajaRequest = new RegistrarBajaMaterialRequest(
                new BigDecimal("15.00"),
                CausaFracaso.PLAGA_ENFERMEDAD,
                "Lote con plaga de pulgones"
        );

        BajaMaterialVegetal bajaGuardada = new BajaMaterialVegetal();
        bajaGuardada.setId(100L);
        bajaGuardada.setBodegaMaterial(material);
        bajaGuardada.setCantidadBaja(new BigDecimal("15.00"));
        bajaGuardada.setCausa(CausaFracaso.PLAGA_ENFERMEDAD);
        bajaGuardada.setObservacion("Lote con plaga de pulgones");
        bajaGuardada.setFechaBaja(LocalDateTime.now());

        BajaMaterialResponse respuestaEsperada = new BajaMaterialResponse(
                100L,
                10L,
                new BigDecimal("15.00"),
                CausaFracaso.PLAGA_ENFERMEDAD,
                "Lote con plaga de pulgones",
                bajaGuardada.getFechaBaja()
        );

        when(bodegaRepository.findById(10L)).thenReturn(Optional.of(material));
        when(bodegaRepository.save(material)).thenReturn(material);
        when(bajaMaterialVegetalRepository.save(any(BajaMaterialVegetal.class))).thenReturn(bajaGuardada);
        when(bodegaMapper.toBajaMaterialResponse(bajaGuardada)).thenReturn(respuestaEsperada);

        BajaMaterialResponse resultado = bodegaService.registrarBajaMaterial(10L, bajaRequest);

        assertThat(resultado).isNotNull();
        assertThat(resultado.id()).isEqualTo(100L);
        assertThat(resultado.bodegaMaterialId()).isEqualTo(10L);
        assertThat(resultado.cantidadBaja()).isEqualTo(new BigDecimal("15.00"));
        assertThat(resultado.causaFracaso()).isEqualTo(CausaFracaso.PLAGA_ENFERMEDAD);
        assertThat(resultado.observacion()).isEqualTo("Lote con plaga de pulgones");
        assertThat(material.getCantidadDisponible()).isEqualByComparingTo("35.00");

        verify(bodegaRepository).save(material);
        verify(bajaMaterialVegetalRepository).save(any(BajaMaterialVegetal.class));
    }

    @Test
    void registrarBajaMaterialLanzaExcepcionCuandoNoExisteMaterial() {
        RegistrarBajaMaterialRequest bajaRequest = new RegistrarBajaMaterialRequest(
                new BigDecimal("10.00"),
                CausaFracaso.CADUCIDAD_PUDRICION,
                null
        );

        when(bodegaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bodegaService.registrarBajaMaterial(99L, bajaRequest))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("El material vegetal indicado no existe en bodega");
    }

    @Test
    void registrarBajaMaterialLanzaExcepcionCuandoSuperaStockDisponible() {
        BodegaMaterialVegetal material = new BodegaMaterialVegetal();
        material.setId(10L);
        material.setComunidad(comunidad);
        material.setPlanta(planta);
        material.setCantidadDisponible(new BigDecimal("10.00"));

        RegistrarBajaMaterialRequest bajaRequest = new RegistrarBajaMaterialRequest(
                new BigDecimal("15.00"),
                CausaFracaso.SEQUIA_FALTA_RIEGO,
                null
        );

        when(bodegaRepository.findById(10L)).thenReturn(Optional.of(material));

        assertThatThrownBy(() -> bodegaService.registrarBajaMaterial(10L, bajaRequest))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La cantidad a dar de baja supera el stock disponible en bodega");
    }

    @Test
    void registrarBajaMaterialValidaComunidadDeResponsable() {
        BodegaMaterialVegetal material = new BodegaMaterialVegetal();
        material.setId(10L);
        material.setComunidad(comunidad);
        material.setPlanta(planta);
        material.setCantidadDisponible(new BigDecimal("50.00"));

        RegistrarBajaMaterialRequest bajaRequest = new RegistrarBajaMaterialRequest(
                new BigDecimal("5.00"),
                CausaFracaso.DANIO_EXTERNO,
                null
        );

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("ajeno@correo.com");
        when(auth.isAuthenticated()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_RESPONSABLE_COMUNIDAD"))).when(auth).getAuthorities();
        when(comunidadRepository.findByResponsableCorreo("ajeno@correo.com")).thenReturn(Optional.empty());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            when(bodegaRepository.findById(10L)).thenReturn(Optional.of(material));

            assertThatThrownBy(() -> bodegaService.registrarBajaMaterial(10L, bajaRequest))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessage("No existe una comunidad asociada al usuario");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
