package com.huertos.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaResumenResponse;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CualidadTermica;
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
}
