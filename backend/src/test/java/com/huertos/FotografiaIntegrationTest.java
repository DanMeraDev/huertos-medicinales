package com.huertos;

import com.huertos.repository.UsuarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.JwtService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class FotografiaIntegrationTest {

    private static final String CORREO_TECNICO = "tecnico.fotos@huertos.gob.ec";
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};

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
                INSERT INTO usuarios (nombre_completo, identificacion, fecha_nacimiento, genero, correo, password_hash, rol)
                VALUES ('Técnico de fotografías', '1799999905', '1990-05-14', 'FEMENINO', ?, 'hash', 'TECNICO_MSP')
                """, CORREO_TECNICO);
    }

    @AfterEach
    void limpiar() {
        jdbcTemplate.update("DELETE FROM usuarios WHERE correo = ?", CORREO_TECNICO);
    }

    @Test
    void elTecnicoSubeUnaFotografiaYCualquieraPuedeDescargarla() throws Exception {
        String token = jwtService.generarToken(usuarioRepository.findByCorreo(CORREO_TECNICO).orElseThrow());

        String respuesta = mockMvc.perform(multipart("/api/msp/plantas/fotografia")
                        .file(new MockMultipartFile("archivo", "hoja.png", "image/png", PNG))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String ruta = JsonPath.read(respuesta, "$.fotografiaUrl");

        mockMvc.perform(get("/api/publico/fotos/" + ruta))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(PNG));
    }

    @Test
    void entregaLasFotosInicialesDelCatalogo() throws Exception {
        mockMvc.perform(get("/api/publico/fotos/plantas/manzanilla.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }
}
