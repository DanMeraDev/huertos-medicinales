package com.huertos.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.FiltrosPlantaRequest;
import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import com.huertos.service.PlantaService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantaPublicoController.class)
@Import(SecurityConfig.class)
class PlantaPublicoControllerTest {

    private static final PlantaResumenResponse MANZANILLA = new PlantaResumenResponse(1L, "Manzanilla", null,
            "Matricaria chamomilla", CualidadTermica.CALIENTE,
            Set.of(CategoriaUso.SEDANTE, CategoriaUso.ANTIESPASMODICA), "plantas/manzanilla.jpg");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlantaService plantaService;

    @Test
    void listaElCatalogoSinTokenConLaPaginacionPorDefecto() throws Exception {
        when(plantaService.listarPlantasPublicas(0, 12))
                .thenReturn(new PaginaResponse<>(List.of(MANZANILLA), 0, 12, 1, 1));

        mockMvc.perform(get("/api/publico/plantas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].id").value(1))
                .andExpect(jsonPath("$.contenido[0].nombreComun").value("Manzanilla"))
                .andExpect(jsonPath("$.contenido[0].nombreCientifico").value("Matricaria chamomilla"))
                .andExpect(jsonPath("$.contenido[0].cualidadTermica").value("CALIENTE"))
                .andExpect(jsonPath("$.contenido[0].categorias", containsInAnyOrder("SEDANTE", "ANTIESPASMODICA")))
                .andExpect(jsonPath("$.contenido[0].fotografiaUrl").value("plantas/manzanilla.jpg"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanio").value(12))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void pasaLaPaginaYElTamanioPedidosAlServicio() throws Exception {
        when(plantaService.listarPlantasPublicas(2, 100))
                .thenReturn(new PaginaResponse<>(List.of(), 2, 100, 20, 1));

        mockMvc.perform(get("/api/publico/plantas").param("page", "2").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido").isEmpty())
                .andExpect(jsonPath("$.pagina").value(2))
                .andExpect(jsonPath("$.tamanio").value(100));
    }

    @Test
    void buscaPlantasSinTokenYDevuelveLaListaEnJson() throws Exception {
        when(plantaService.buscarPorNombre("manza")).thenReturn(List.of(MANZANILLA));

        mockMvc.perform(get("/api/publico/plantas/buscar").param("criterio", "manza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombreComun").value("Manzanilla"))
                .andExpect(jsonPath("$[0].nombreCientifico").value("Matricaria chamomilla"))
                .andExpect(jsonPath("$[0].categorias", containsInAnyOrder("SEDANTE", "ANTIESPASMODICA")))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void sinCriterioRespondeSolicitudIncorrecta() throws Exception {
        mockMvc.perform(get("/api/publico/plantas/buscar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void conUnCriterioCortoRespondeSolicitudIncorrectaConElMensaje() throws Exception {
        when(plantaService.buscarPorNombre("a"))
                .thenThrow(new ReglaNegocioException("El criterio de búsqueda debe tener entre 2 y 100 caracteres"));

        mockMvc.perform(get("/api/publico/plantas/buscar").param("criterio", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El criterio de búsqueda debe tener entre 2 y 100 caracteres"));
    }

    @Test
    void filtraPlantasSinTokenYDevuelveLaListaEnJson() throws Exception {
        when(plantaService.filtrarPlantas(new FiltrosPlantaRequest(null, null, null, null, null, null, null)))
                .thenReturn(List.of(MANZANILLA));

        mockMvc.perform(get("/api/publico/plantas/filtrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombreComun").value("Manzanilla"))
                .andExpect(jsonPath("$[0].cualidadTermica").value("CALIENTE"))
                .andExpect(jsonPath("$[0].categorias", containsInAnyOrder("SEDANTE", "ANTIESPASMODICA")))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void pasaTodosLosFiltrosAlServicioEnElRecord() throws Exception {
        when(plantaService.filtrarPlantas(new FiltrosPlantaRequest(CualidadTermica.CALIENTE, 2800, "Sierra",
                CategoriaUso.SEDANTE, CicloCultivo.CORTO, OrigenPlanta.INTRODUCIDA, "manza")))
                .thenReturn(List.of(MANZANILLA));

        mockMvc.perform(get("/api/publico/plantas/filtrar")
                        .param("cualidad", "CALIENTE")
                        .param("altitud", "2800")
                        .param("region", "Sierra")
                        .param("categoria", "SEDANTE")
                        .param("ciclo", "CORTO")
                        .param("origen", "INTRODUCIDA")
                        .param("criterio", "manza"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreComun").value("Manzanilla"));
    }

    @Test
    void conUnaAltitudFueraDeRangoRespondeSolicitudIncorrectaConElMensaje() throws Exception {
        when(plantaService.filtrarPlantas(new FiltrosPlantaRequest(null, 7000, null, null, null, null, null)))
                .thenThrow(new ReglaNegocioException("La altitud debe estar entre 0 y 6000 msnm"));

        mockMvc.perform(get("/api/publico/plantas/filtrar").param("altitud", "7000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La altitud debe estar entre 0 y 6000 msnm"));
    }

    @Test
    void conUnaCualidadInexistenteRespondeSolicitudIncorrecta() throws Exception {
        mockMvc.perform(get("/api/publico/plantas/filtrar").param("cualidad", "TIBIA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cualidad").exists());
        verifyNoInteractions(plantaService);
    }

    @Test
    void devuelveLaFichaTecnicaSinTokenConLosEnumsComoTexto() throws Exception {
        when(plantaService.obtenerDetalle(7L)).thenReturn(new PlantaDetalleResponse(7L, "Cola de caballo",
                "Caballo chupa", "Equisetum giganteum", "Equisetaceae", CualidadTermica.FRIA, CicloCultivo.LARGO,
                OrigenPlanta.NATIVA, 0, 3500, "Sierra, Amazonía", "Para problemas de riñón",
                "Hervir el tallo 15 minutos", null, Set.of(CategoriaUso.DIURETICA), "plantas/cola-de-caballo.jpg"));

        mockMvc.perform(get("/api/publico/plantas/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombreComun").value("Cola de caballo"))
                .andExpect(jsonPath("$.nombreKichwa").value("Caballo chupa"))
                .andExpect(jsonPath("$.nombreCientifico").value("Equisetum giganteum"))
                .andExpect(jsonPath("$.familiaBotanica").value("Equisetaceae"))
                .andExpect(jsonPath("$.cualidadTermica").value("FRIA"))
                .andExpect(jsonPath("$.cicloCultivo").value("LARGO"))
                .andExpect(jsonPath("$.origen").value("NATIVA"))
                .andExpect(jsonPath("$.altitudMinMsnm").value(0))
                .andExpect(jsonPath("$.altitudMaxMsnm").value(3500))
                .andExpect(jsonPath("$.regionesDistribucion").value("Sierra, Amazonía"))
                .andExpect(jsonPath("$.usosMedicinales").value("Para problemas de riñón"))
                .andExpect(jsonPath("$.preparacionDosificacion").value("Hervir el tallo 15 minutos"))
                .andExpect(jsonPath("$.contraindicaciones").value(nullValue()))
                .andExpect(jsonPath("$.categorias", contains("DIURETICA")))
                .andExpect(jsonPath("$.fotografiaUrl").value("plantas/cola-de-caballo.jpg"));
    }

    @Test
    void unaFichaNoDisponibleRespondeNoEncontradoConElMensaje() throws Exception {
        when(plantaService.obtenerDetalle(99L))
                .thenThrow(new RecursoNoEncontradoException("La ficha solicitada no está disponible"));

        mockMvc.perform(get("/api/publico/plantas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("La ficha solicitada no está disponible"));
    }

    @Test
    void unIdNoNumericoRespondeSolicitudIncorrecta() throws Exception {
        mockMvc.perform(get("/api/publico/plantas/abc"))
                .andExpect(status().isBadRequest());
    }
}
