package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.ComunidadResumenResponse;
import com.huertos.dto.ContextoBodegaResponse;
import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.BodegaMapper;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.Comunidad;
import com.huertos.model.Planta;
import com.huertos.model.Rol;
import com.huertos.model.TipoMaterialVegetal;
import com.huertos.model.UnidadMedida;
import com.huertos.repository.BodegaMaterialVegetalRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.impl.BodegaServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BodegaServiceImplTest {

    @Mock
    private BodegaMaterialVegetalRepository bodegaRepository;

    @Mock
    private ComunidadRepository comunidadRepository;

    @Mock
    private PlantaRepository plantaRepository;

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
    void listarMaterialPorComunidadExitoso() {
        BodegaMaterialVegetal entidad = new BodegaMaterialVegetal();
        entidad.setId(100L);
        entidad.setComunidad(comunidad);
        entidad.setPlanta(planta);
        entidad.setTipoMaterial(TipoMaterialVegetal.SEMILLAS);
        entidad.setCantidadDisponible(new BigDecimal("20.00"));

        MaterialVegetalResponse respuesta = new MaterialVegetalResponse(
                100L, 1L, 10L, "Manzanilla", "Matricaria chamomilla",
                TipoMaterialVegetal.SEMILLAS, new BigDecimal("20.00"), UnidadMedida.GRAMOS,
                "Lote A", LocalDate.now()
        );

        when(bodegaRepository.findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(1L))
                .thenReturn(List.of(entidad));
        when(bodegaMapper.toListMaterialVegetalResponse(List.of(entidad)))
                .thenReturn(List.of(respuesta));

        List<MaterialVegetalResponse> resultado = bodegaService.listarMaterialPorComunidad(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombreComunPlanta()).isEqualTo("Manzanilla");
    }

    @Test
    void listarMaterialPorComunidadParaResponsableValidaComunidadPropia() {
        List<BodegaMaterialVegetal> listaVacia = List.of();
        when(comunidadRepository.findByResponsableCorreo("responsable@correo.com"))
                .thenReturn(Optional.of(comunidad));
        when(bodegaRepository.findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(1L))
                .thenReturn(listaVacia);
        when(bodegaMapper.toListMaterialVegetalResponse(listaVacia)).thenReturn(List.of());

        List<MaterialVegetalResponse> resultado = bodegaService.listarMaterialPorComunidad(
                1L, "responsable@correo.com", false
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    void listarMaterialPorComunidadParaResponsableRechazaComunidadAjena() {
        when(comunidadRepository.findByResponsableCorreo("responsable@correo.com"))
                .thenReturn(Optional.of(comunidad));

        assertThatThrownBy(() -> bodegaService.listarMaterialPorComunidad(2L, "responsable@correo.com", false))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessage("No tiene permisos para consultar el inventario de otra comunidad");
    }

    @Test
    void listarMaterialPorComunidadParaTecnicoMspPermiteCualquierComunidad() {
        List<BodegaMaterialVegetal> listaVacia = List.of();
        when(bodegaRepository.findByComunidadIdAndActivoTrueOrderByPlantaNombreComunAsc(2L))
                .thenReturn(listaVacia);
        when(bodegaMapper.toListMaterialVegetalResponse(listaVacia)).thenReturn(List.of());

        List<MaterialVegetalResponse> resultado = bodegaService.listarMaterialPorComunidad(
                2L, "tecnico@msp.gob.ec", true
        );

        assertThat(resultado).isEmpty();
    }

    @Test
    void obtenerContextoDeResponsableDevuelveSoloSuComunidad() {
        List<ComunidadResumenResponse> resumen = List.of(new ComunidadResumenResponse(1L, "Comunidad San Pedro"));
        when(comunidadRepository.findByResponsableCorreo("responsable@correo.com")).thenReturn(Optional.of(comunidad));
        when(bodegaMapper.toListComunidadResumenResponse(List.of(comunidad))).thenReturn(resumen);

        ContextoBodegaResponse contexto = bodegaService.obtenerContexto("responsable@correo.com", false);

        assertThat(contexto.rol()).isEqualTo(Rol.RESPONSABLE_COMUNIDAD);
        assertThat(contexto.comunidades()).isEqualTo(resumen);
    }

    @Test
    void obtenerContextoDeResponsableSinComunidadLanzaExcepcion() {
        when(comunidadRepository.findByResponsableCorreo("ajeno@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bodegaService.obtenerContexto("ajeno@correo.com", false))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe una comunidad asociada al usuario");
    }

    @Test
    void obtenerContextoDeTecnicoMspDevuelveComunidadesActivas() {
        Comunidad otra = new Comunidad();
        otra.setId(2L);
        otra.setNombre("Comunidad Santa Clara");
        List<ComunidadResumenResponse> resumen = List.of(
                new ComunidadResumenResponse(1L, "Comunidad San Pedro"),
                new ComunidadResumenResponse(2L, "Comunidad Santa Clara"));
        when(comunidadRepository.findByActivaTrueOrderByNombreAsc()).thenReturn(List.of(comunidad, otra));
        when(bodegaMapper.toListComunidadResumenResponse(List.of(comunidad, otra))).thenReturn(resumen);

        ContextoBodegaResponse contexto = bodegaService.obtenerContexto("tecnico@msp.gob.ec", true);

        assertThat(contexto.rol()).isEqualTo(Rol.TECNICO_MSP);
        assertThat(contexto.comunidades()).isEqualTo(resumen);
    }
}
