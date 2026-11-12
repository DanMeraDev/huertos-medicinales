package com.huertos;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.model.CualidadTermica;
import com.huertos.model.Planta;
import com.huertos.repository.PlantaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PlantaPublicoIntegrationTest {

    private static final String NOMBRE_FICTICIO = "Hierba ficticia de ficha";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlantaRepository plantaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM plantas WHERE nombre_comun = ?", NOMBRE_FICTICIO);
    }

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

    @Test
    void devuelveLaFichaTecnicaDeUnaPlantaDelMsp() throws Exception {
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM plantas WHERE nombre_comun = ?", Long.class, "Cola de caballo");

        mockMvc.perform(get("/api/publico/plantas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
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
                .andExpect(jsonPath("$.usosMedicinales").isNotEmpty())
                .andExpect(jsonPath("$.preparacionDosificacion").isNotEmpty())
                .andExpect(jsonPath("$.contraindicaciones").isNotEmpty())
                .andExpect(jsonPath("$.categorias", contains("DIURETICA")))
                .andExpect(jsonPath("$.fotografiaUrl").value("plantas/cola-de-caballo.jpg"));
    }

    @Test
    void unaPlantaInactivaRespondeNoEncontrado() throws Exception {
        Planta inactiva = new Planta();
        inactiva.setNombreComun(NOMBRE_FICTICIO);
        inactiva.setNombreCientifico("Planta inventada");
        inactiva.setFamiliaBotanica("Ficticiaceae");
        inactiva.setCualidadTermica(CualidadTermica.FRIA);
        inactiva.setUsosMedicinales("Ninguno");
        inactiva.setActiva(false);
        Long id = plantaRepository.save(inactiva).getId();

        mockMvc.perform(get("/api/publico/plantas/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("La ficha solicitada no está disponible"));
    }
}
