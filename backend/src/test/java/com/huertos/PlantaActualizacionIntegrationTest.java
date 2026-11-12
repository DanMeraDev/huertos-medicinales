package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.JwtService;
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
class PlantaActualizacionIntegrationTest {

    private static final String CORREO_TECNICO = "tecnico.edicion@huertos.gob.ec";
    private static final String NOMBRE_ORIGINAL = "Herba editanda ficticia";
    private static final String NOMBRE_EDITADO = "Herba editata ficticia";
    private static final String NOMBRE_DE_OTRA = "Herba altera ficticia";
    private static final String SOLICITUD = """
            {
              "nombreComun": "Hierba ficticia editada",
              "nombreKichwa": "Yura musuk",
              "nombreCientifico": "  Herba editata ficticia  ",
              "familiaBotanica": "Editaceae",
              "cualidadTermica": "CALIENTE",
              "cicloCultivo": "LARGO",
              "origen": "ENDEMICA",
              "altitudMinMsnm": 1000,
              "altitudMaxMsnm": 1800,
              "regionesDistribucion": "Amazonía",
              "usosMedicinales": "Calma la tos",
              "preparacionDosificacion": "  ",
              "categorias": ["EXPECTORANTE", "ANTIINFLAMATORIA"]
            }
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
                VALUES ('Técnico de edición', '1799999906', '1990-05-14', 'MASCULINO', ?, 'hash', 'TECNICO_MSP')
                """, CORREO_TECNICO);
        tecnicoId = jdbcTemplate.queryForObject("SELECT id FROM usuarios WHERE correo = ?", Long.class, CORREO_TECNICO);
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("""
                DELETE FROM planta_categorias
                WHERE planta_id IN (SELECT id FROM plantas WHERE nombre_cientifico IN (?, ?, ?))
                """, NOMBRE_ORIGINAL, NOMBRE_EDITADO, NOMBRE_DE_OTRA);
        jdbcTemplate.update("DELETE FROM plantas WHERE nombre_cientifico IN (?, ?, ?)",
                NOMBRE_ORIGINAL, NOMBRE_EDITADO, NOMBRE_DE_OTRA);
        jdbcTemplate.update("DELETE FROM usuarios WHERE correo = ?", CORREO_TECNICO);
    }

    @Test
    void losCambiosSeReflejanEnLaFichaPublica() throws Exception {
        Long id = insertarPlanta(NOMBRE_ORIGINAL);

        actualizar(id, SOLICITUD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombreCientifico").value(NOMBRE_EDITADO))
                .andExpect(jsonPath("$.preparacionDosificacion").doesNotExist());

        mockMvc.perform(get("/api/publico/plantas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComun").value("Hierba ficticia editada"))
                .andExpect(jsonPath("$.nombreCientifico").value(NOMBRE_EDITADO))
                .andExpect(jsonPath("$.cualidadTermica").value("CALIENTE"))
                .andExpect(jsonPath("$.regionesDistribucion").value("Amazonía"))
                .andExpect(jsonPath("$.categorias", containsInAnyOrder("EXPECTORANTE", "ANTIINFLAMATORIA")));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT actualizado_por_id FROM plantas WHERE id = ?", Long.class, id)).isEqualTo(tecnicoId);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT activa FROM plantas WHERE id = ?", Boolean.class, id)).isTrue();
    }

    @Test
    void rechazaElNombreCientificoDeUnaPlantaDadaDeBajaConOtrasMayusculas() throws Exception {
        Long id = insertarPlanta(NOMBRE_ORIGINAL);
        Long otraId = insertarPlanta(NOMBRE_DE_OTRA);
        jdbcTemplate.update("UPDATE plantas SET activa = false WHERE id = ?", otraId);

        actualizar(id, SOLICITUD.replace("Herba editata ficticia", "HERBA ALTERA FICTICIA"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Nombre científico ya registrado en otra planta"));
    }

    @Test
    void rechazaEditarUnaPlantaDadaDeBaja() throws Exception {
        Long id = insertarPlanta(NOMBRE_ORIGINAL);
        jdbcTemplate.update("UPDATE plantas SET activa = false WHERE id = ?", id);

        actualizar(id, SOLICITUD)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("La planta no existe o fue dada de baja"));
    }

    private Long insertarPlanta(String nombreCientifico) {
        jdbcTemplate.update("""
                INSERT INTO plantas (nombre_comun, nombre_cientifico, familia_botanica, cualidad_termica,
                                     usos_medicinales)
                VALUES ('Hierba ficticia original', ?, 'Ficticiaceae', 'FRIA', 'Alivia el dolor')
                """, nombreCientifico);
        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM plantas WHERE nombre_cientifico = ?", Long.class, nombreCientifico);
        jdbcTemplate.update("INSERT INTO planta_categorias (planta_id, categoria) VALUES (?, 'SEDANTE')", id);
        return id;
    }

    private ResultActions actualizar(Long id, String solicitud) throws Exception {
        String token = jwtService.generarToken(CORREO_TECNICO, "TECNICO_MSP");
        return mockMvc.perform(put("/api/msp/plantas/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(solicitud));
    }
}
