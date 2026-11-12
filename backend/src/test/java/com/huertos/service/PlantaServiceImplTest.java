package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.mapper.PlantaMapper;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import com.huertos.model.Planta;
import com.huertos.repository.PlantaRepository;
import com.huertos.service.impl.PlantaServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class PlantaServiceImplTest {

    private static final Sort POR_NOMBRE_COMUN = Sort.by(Sort.Direction.ASC, "nombreComun", "id");

    @Mock
    private PlantaRepository plantaRepository;

    @Captor
    private ArgumentCaptor<Pageable> paginacion;

    private PlantaService servicio;

    @BeforeEach
    void crearServicio() {
        servicio = new PlantaServiceImpl(plantaRepository, Mappers.getMapper(PlantaMapper.class));
    }

    @Test
    void consultaSoloPlantasActivasOrdenadasPorNombreComun() {
        when(plantaRepository.findAllByActivaTrue(any())).thenReturn(Page.empty());

        servicio.listarPlantasPublicas(2, 12);

        verify(plantaRepository).findAllByActivaTrue(paginacion.capture());
        assertThat(paginacion.getValue().getPageNumber()).isEqualTo(2);
        assertThat(paginacion.getValue().getPageSize()).isEqualTo(12);
        assertThat(paginacion.getValue().getSort()).isEqualTo(POR_NOMBRE_COMUN);
    }

    @Test
    void limitaElTamanioDePaginaACien() {
        when(plantaRepository.findAllByActivaTrue(any())).thenReturn(Page.empty());

        servicio.listarPlantasPublicas(0, 500);

        verify(plantaRepository).findAllByActivaTrue(paginacion.capture());
        assertThat(paginacion.getValue().getPageSize()).isEqualTo(100);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void unTamanioMenorAUnoSeConsultaConTamanioUno(int tamanio) {
        when(plantaRepository.findAllByActivaTrue(any())).thenReturn(Page.empty());

        servicio.listarPlantasPublicas(0, tamanio);

        verify(plantaRepository).findAllByActivaTrue(paginacion.capture());
        assertThat(paginacion.getValue().getPageSize()).isEqualTo(1);
    }

    @Test
    void unaPaginaNegativaSeConsultaComoLaPrimera() {
        when(plantaRepository.findAllByActivaTrue(any())).thenReturn(Page.empty());

        servicio.listarPlantasPublicas(-3, 12);

        verify(plantaRepository).findAllByActivaTrue(paginacion.capture());
        assertThat(paginacion.getValue().getPageNumber()).isZero();
    }

    @Test
    void armaLaPaginaConElResumenDeLasPlantas() {
        Planta manzanilla = planta(1L, "Manzanilla", "Matricaria chamomilla",
                Set.of(CategoriaUso.SEDANTE, CategoriaUso.ANTIESPASMODICA), "plantas/manzanilla.jpg");
        manzanilla.setNombreKichwa("Manzanilla kiwa");
        Planta menta = planta(2L, "Menta", "Mentha x piperita", Set.of(CategoriaUso.ESTIMULANTE), "plantas/menta.jpg");
        when(plantaRepository.findAllByActivaTrue(any()))
                .thenReturn(new PageImpl<>(List.of(manzanilla, menta), PageRequest.of(1, 2, POR_NOMBRE_COMUN), 5));

        PaginaResponse<PlantaResumenResponse> respuesta = servicio.listarPlantasPublicas(1, 2);

        assertThat(respuesta.contenido()).containsExactly(
                new PlantaResumenResponse(1L, "Manzanilla", "Manzanilla kiwa", "Matricaria chamomilla",
                        CualidadTermica.CALIENTE, Set.of(CategoriaUso.SEDANTE, CategoriaUso.ANTIESPASMODICA),
                        "plantas/manzanilla.jpg"),
                new PlantaResumenResponse(2L, "Menta", null, "Mentha x piperita",
                        CualidadTermica.CALIENTE, Set.of(CategoriaUso.ESTIMULANTE), "plantas/menta.jpg"));
        assertThat(respuesta.pagina()).isEqualTo(1);
        assertThat(respuesta.tamanio()).isEqualTo(2);
        assertThat(respuesta.totalElementos()).isEqualTo(5);
        assertThat(respuesta.totalPaginas()).isEqualTo(3);
    }

    @Test
    void sinPlantasActivasDevuelveUnaPaginaVacia() {
        when(plantaRepository.findAllByActivaTrue(any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 12, POR_NOMBRE_COMUN), 0));

        PaginaResponse<PlantaResumenResponse> respuesta = servicio.listarPlantasPublicas(0, 12);

        assertThat(respuesta.contenido()).isEmpty();
        assertThat(respuesta.totalElementos()).isZero();
        assertThat(respuesta.totalPaginas()).isZero();
    }

    @Test
    void buscaConElCriterioSinEspaciosAlInicioNiAlFinal() {
        when(plantaRepository.buscarActivasPorNombre("matico")).thenReturn(List.of());

        servicio.buscarPorNombre("  matico  ");

        verify(plantaRepository).buscarActivasPorNombre("matico");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " a ", "   "})
    void rechazaUnCriterioDeMenosDeDosCaracteresSinConsultar(String criterio) {
        assertThatThrownBy(() -> servicio.buscarPorNombre(criterio))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El criterio de búsqueda debe tener entre 2 y 100 caracteres");
        verifyNoInteractions(plantaRepository);
    }

    @Test
    void rechazaUnCriterioDeMasDeCienCaracteresSinConsultar() {
        String criterio = "a".repeat(101);

        assertThatThrownBy(() -> servicio.buscarPorNombre(criterio))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("El criterio de búsqueda debe tener entre 2 y 100 caracteres");
        verifyNoInteractions(plantaRepository);
    }

    @Test
    void aceptaUnCriterioDeCienCaracteres() {
        String criterio = "a".repeat(100);
        when(plantaRepository.buscarActivasPorNombre(criterio)).thenReturn(List.of());

        assertThat(servicio.buscarPorNombre(criterio)).isEmpty();
    }

    @Test
    void devuelveElResumenDeLasPlantasEncontradas() {
        Planta manzanilla = planta(1L, "Manzanilla", "Matricaria chamomilla",
                Set.of(CategoriaUso.SEDANTE), "plantas/manzanilla.jpg");
        Planta matico = planta(6L, "Matico", "Aristeguietia glutinosa",
                Set.of(CategoriaUso.CICATRIZANTE), "plantas/matico.jpg");
        when(plantaRepository.buscarActivasPorNombre("ma")).thenReturn(List.of(manzanilla, matico));

        List<PlantaResumenResponse> resultado = servicio.buscarPorNombre("ma");

        assertThat(resultado).containsExactly(
                new PlantaResumenResponse(1L, "Manzanilla", null, "Matricaria chamomilla",
                        CualidadTermica.CALIENTE, Set.of(CategoriaUso.SEDANTE), "plantas/manzanilla.jpg"),
                new PlantaResumenResponse(6L, "Matico", null, "Aristeguietia glutinosa",
                        CualidadTermica.CALIENTE, Set.of(CategoriaUso.CICATRIZANTE), "plantas/matico.jpg"));
    }

    @Test
    void devuelveLaFichaTecnicaConTodosLosCamposDeUnaPlantaActiva() {
        Planta colaDeCaballo = planta(7L, "Cola de caballo", "Equisetum giganteum",
                Set.of(CategoriaUso.DIURETICA), "plantas/cola-de-caballo.jpg");
        colaDeCaballo.setNombreKichwa("Caballo chupa");
        colaDeCaballo.setFamiliaBotanica("Equisetaceae");
        colaDeCaballo.setCualidadTermica(CualidadTermica.FRIA);
        colaDeCaballo.setCicloCultivo(CicloCultivo.LARGO);
        colaDeCaballo.setOrigen(OrigenPlanta.NATIVA);
        colaDeCaballo.setAltitudMinMsnm(0);
        colaDeCaballo.setAltitudMaxMsnm(3500);
        colaDeCaballo.setRegionesDistribucion("Sierra, Amazonía");
        colaDeCaballo.setUsosMedicinales("Para problemas de riñón");
        colaDeCaballo.setPreparacionDosificacion("Hervir el tallo 15 minutos");
        colaDeCaballo.setContraindicaciones("No usar en embarazo");
        when(plantaRepository.findByIdAndActivaTrue(7L)).thenReturn(Optional.of(colaDeCaballo));

        PlantaDetalleResponse ficha = servicio.obtenerDetalle(7L);

        assertThat(ficha).isEqualTo(new PlantaDetalleResponse(7L, "Cola de caballo", "Caballo chupa",
                "Equisetum giganteum", "Equisetaceae", CualidadTermica.FRIA, CicloCultivo.LARGO,
                OrigenPlanta.NATIVA, 0, 3500, "Sierra, Amazonía", "Para problemas de riñón",
                "Hervir el tallo 15 minutos", "No usar en embarazo", Set.of(CategoriaUso.DIURETICA),
                "plantas/cola-de-caballo.jpg"));
    }

    @Test
    void devuelveComoNuloLosCamposQueLaPlantaNoTiene() {
        when(plantaRepository.findByIdAndActivaTrue(1L)).thenReturn(Optional.of(
                planta(1L, "Manzanilla", "Matricaria chamomilla", Set.of(), null)));

        PlantaDetalleResponse ficha = servicio.obtenerDetalle(1L);

        assertThat(ficha.nombreKichwa()).isNull();
        assertThat(ficha.cicloCultivo()).isNull();
        assertThat(ficha.altitudMinMsnm()).isNull();
        assertThat(ficha.contraindicaciones()).isNull();
        assertThat(ficha.fotografiaUrl()).isNull();
    }

    @Test
    void consultaLaFichaSoloEntreLasPlantasActivas() {
        when(plantaRepository.findByIdAndActivaTrue(3L)).thenReturn(Optional.of(
                planta(3L, "Toronjil", "Melissa officinalis", Set.of(CategoriaUso.SEDANTE), null)));

        servicio.obtenerDetalle(3L);

        verify(plantaRepository).findByIdAndActivaTrue(3L);
        verifyNoMoreInteractions(plantaRepository);
    }

    @Test
    void unaPlantaInexistenteOInactivaLanzaRecursoNoEncontrado() {
        when(plantaRepository.findByIdAndActivaTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtenerDetalle(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La ficha solicitada no está disponible");
    }

    private Planta planta(Long id, String nombreComun, String nombreCientifico, Set<CategoriaUso> categorias,
            String fotografiaUrl) {
        Planta planta = new Planta();
        planta.setId(id);
        planta.setNombreComun(nombreComun);
        planta.setNombreCientifico(nombreCientifico);
        planta.setCualidadTermica(CualidadTermica.CALIENTE);
        planta.setCategorias(categorias);
        planta.setFotografiaUrl(fotografiaUrl);
        return planta;
    }
}
