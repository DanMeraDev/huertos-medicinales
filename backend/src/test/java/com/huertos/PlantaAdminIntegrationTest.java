package com.huertos;

import com.huertos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.JwtService;
import org.junit.jupiter.api.AfterEach;
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
class PlantaAdminIntegrationTest {

    private static final String CORREO_TECNICO = "tecnico.catalogo@huertos.gob.ec";
    private static final String NOMBRE_CIENTIFICO = "Herba registrata ficticia";
    private static final String SOLICITUD = """
            {
              "nombreComun": "Hierba ficticia registrada",
              "nombreKichwa": "  ",
              "nombreCientifico": "  Herba registrata ficticia  ",
              "familiaBotanica": "Ficticiaceae",
              "cualidadTermica": "FRIA",
              "cicloCultivo": "CORTO",
              "origen": "NATIVA",
              "altitudMinMsnm": 2000,
              "altitudMaxMsnm": 3200,
              "regionesDistribucion": "Costa, Sierra",
              "usosMedicinales": "Alivia el dolor de estómago",
              "categorias": ["SEDANTE", "DIURETICA"]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void insertarTecnico() {
        jdbcTemplate.update("""
                INSERT INTO usuarios (nombre_completo, identificacion, fecha_nacimiento, genero, correo, password_hash, rol, requiere_cambio_clave)
                VALUES ('Técnico de catálogo', '1799999904', '1990-05-14', 'FEMENINO', ?, 'hash', 'TECNICO_MSP', FALSE)
                """, CORREO_TECNICO);
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM usuarios WHERE correo = ?", CORREO_TECNICO);
        jdbcTemplate.update("""
                DELETE FROM planta_categorias
                WHERE planta_id IN (SELECT id FROM plantas WHERE nombre_cientifico = ?)
                """, NOMBRE_CIENTIFICO);
        jdbcTemplate.update("DELETE FROM plantas WHERE nombre_cientifico = ?", NOMBRE_CIENTIFICO);
    }

    @Test
    void laPlantaRegistradaQuedaDisponibleEnElCatalogoPublico() throws Exception {
        String ubicacion = registrar(SOLICITUD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreCientifico").value(NOMBRE_CIENTIFICO))
                .andExpect(jsonPath("$.nombreKichwa").doesNotExist())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);

        assertThat(ubicacion).matches("http://localhost/api/publico/plantas/\\d+");
        mockMvc.perform(get(ubicacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComun").value("Hierba ficticia registrada"))
                .andExpect(jsonPath("$.categorias.length()").value(2));
        mockMvc.perform(get("/api/publico/plantas").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[*].nombreCientifico", hasItem(NOMBRE_CIENTIFICO)));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT activa FROM plantas WHERE nombre_cientifico = ?", Boolean.class, NOMBRE_CIENTIFICO))
                .isTrue();
    }

    @Test
    void rechazaElMismoNombreCientificoConOtrasMayusculas() throws Exception {
        registrar(SOLICITUD).andExpect(status().isCreated());

        registrar(SOLICITUD.replace("Herba registrata ficticia", "HERBA REGISTRATA FICTICIA"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El nombre científico ya existe en el catálogo"));
    }

    private ResultActions registrar(String solicitud) throws Exception {
        String token = jwtService.generarToken(usuarioRepository.findByCorreo(CORREO_TECNICO).orElseThrow());
        return mockMvc.perform(post("/api/msp/plantas")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(solicitud));
    }
}
