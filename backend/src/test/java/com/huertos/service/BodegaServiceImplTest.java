package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.mapper.BodegaMapper;
import com.huertos.model.BodegaMaterialVegetal;
import com.huertos.model.Comunidad;
import com.huertos.model.Planta;
import com.huertos.model.TipoMaterialVegetal;
import com.huertos.model.UnidadMedida;
import com.huertos.repository.BodegaMaterialVegetalRepository;
import com.huertos.repository.ComunidadRepository;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.impl.BodegaServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
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
}
