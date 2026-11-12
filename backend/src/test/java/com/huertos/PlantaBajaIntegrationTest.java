package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.JwtService;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PlantaBajaIntegrationTest {

    private static final String CORREO_TECNICO = "tecnico.baja@huertos.gob.ec";
    private static final String NOMBRE_COMUN = "Hierba retirable ficticia";
    private static final String NOMBRE_CIENTIFICO = "Herba retirabilis ficticia";
    private static final String SOLICITUD = """
            { "motivo": "  Registro duplicado  " }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long tecnicoId;

    @BeforeEach
    void insertarTecnico() {
        jdbcTemplate.update("""
                INSERT INTO usuarios (nombre_completo, identificacion, fecha_nacimiento, genero, correo, password_hash, rol)
                VALUES ('Técnico de bajas', '1799999907', '1990-05-14', 'FEMENINO', ?, 'hash', 'TECNICO_MSP')
                """, CORREO_TECNICO);
        tecnicoId = jdbcTemplate.queryForObject("SELECT id FROM usuarios WHERE correo = ?", Long.class, CORREO_TECNICO);
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("""
                DELETE FROM planta_categorias
                WHERE planta_id IN (SELECT id FROM plantas WHERE nombre_cientifico = ?)
                """, NOMBRE_CIENTIFICO);
        jdbcTemplate.update("DELETE FROM plantas WHERE nombre_cientifico = ?", NOMBRE_CIENTIFICO);
        jdbcTemplate.update("DELETE FROM usuarios WHERE correo = ?", CORREO_TECNICO);
    }

    @Test
    void laPlantaDadaDeBajaDejaDeAparecerEnElCatalogoPublico() throws Exception {
        Long id = insertarPlanta();
        mockMvc.perform(get("/api/publico/plantas/buscar").param("criterio", "retirable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        darDeBaja(id).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/publico/plantas").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[*].nombreComun", not(hasItem(NOMBRE_COMUN))));
        mockMvc.perform(get("/api/publico/plantas/buscar").param("criterio", "retirable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/publico/plantas/filtrar").param("cualidad", "FRIA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombreComun", not(hasItem(NOMBRE_COMUN))));
        mockMvc.perform(get("/api/publico/plantas/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void conservaLaFilaConLosDatosDeLaBaja() throws Exception {
        Long id = insertarPlanta();

        darDeBaja(id).andExpect(status().isNoContent());

        assertThat(jdbcTemplate.queryForObject("SELECT activa FROM plantas WHERE id = ?", Boolean.class, id))
                .isFalse();
        Map<String, Object> fila = jdbcTemplate.queryForMap("""
                SELECT motivo_baja, fecha_baja, dado_de_baja_por_id, nombre_comun
                FROM plantas WHERE id = ?
                """, id);
        assertThat(fila.get("motivo_baja")).isEqualTo("Registro duplicado");
        assertThat(fila.get("fecha_baja")).isNotNull();
        assertThat(((Number) fila.get("dado_de_baja_por_id")).longValue()).isEqualTo(tecnicoId);
        assertThat(fila.get("nombre_comun")).isEqualTo(NOMBRE_COMUN);
    }

    @Test
    void rechazaDarDeBajaDosVecesLaMismaPlanta() throws Exception {
        Long id = insertarPlanta();
        darDeBaja(id).andExpect(status().isNoContent());

        darDeBaja(id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La planta ya está dada de baja"));
    }

    private Long insertarPlanta() {
        jdbcTemplate.update("""
                INSERT INTO plantas (nombre_comun, nombre_cientifico, familia_botanica, cualidad_termica,
                                     usos_medicinales)
                VALUES (?, ?, 'Ficticiaceae', 'FRIA', 'Alivia el dolor')
                """, NOMBRE_COMUN, NOMBRE_CIENTIFICO);
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM plantas WHERE nombre_cientifico = ?", Long.class, NOMBRE_CIENTIFICO);
        jdbcTemplate.update("INSERT INTO planta_categorias (planta_id, categoria) VALUES (?, 'SEDANTE')", id);
        return id;
    }

    private ResultActions darDeBaja(Long id) throws Exception {
        String token = jwtService.generarToken(CORREO_TECNICO, "TECNICO_MSP");
        return mockMvc.perform(patch("/api/msp/plantas/{id}/baja", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(SOLICITUD));
    }
}
