package com.huertos;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PlantaPublicoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listaLasVeintePlantasDelMspOrdenadasAlfabeticamente() throws Exception {
        mockMvc.perform(get("/api/publico/plantas").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(20))
                .andExpect(jsonPath("$.contenido[*].nombreComun", contains(
                        "Achicoria", "Albahaca", "Cedrón", "Chilca", "Cola de caballo", "Diente de león",
                        "Llantén", "Manzanilla", "Marco", "Matico", "Menta", "Orégano", "Ortiga", "Paico",
                        "Romero", "Ruda", "Sábila", "Toronjil", "Valeriana", "Violeta")))
                .andExpect(jsonPath("$.contenido[0].categorias").isNotEmpty());
    }
}
