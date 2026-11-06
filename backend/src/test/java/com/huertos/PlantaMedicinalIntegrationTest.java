package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PlantaMedicinalIntegrationTest {

    @TempDir
    static Path carpetaFotos;

    @DynamicPropertySource
    static void configurarCarpetaFotos(DynamicPropertyRegistry registro) {
        registro.add("app.fotos.directorio", () -> carpetaFotos.toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void registraLaPlantaConSuFotoYLaFotoSePuedeDescargarSinSesion() throws Exception {
        byte[] imagen = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01, 0x02};
        String datos = """
                {
                  "nombreComun": "Toronjil",
                  "nombreCientifico": "Melissa officinalis",
                  "familiaBotanica": "Lamiaceae",
                  "regionDistribucion": "Sierra: Pichincha",
                  "cicloCultivo": "LARGO",
                  "cualidadTermica": "FRIA",
                  "usosMedicinales": "Calmante"
                }
                """;

        mockMvc.perform(multipart("/api/plantas")
                        .file(new MockMultipartFile("datos", "", MediaType.APPLICATION_JSON_VALUE,
                                datos.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("foto", "toronjil.jpg", MediaType.IMAGE_JPEG_VALUE, imagen)))
                .andExpect(status().isCreated());

        String fotoUrl = jdbcTemplate.queryForObject(
                "SELECT foto_url FROM plantas_medicinales WHERE nombre_cientifico = ?",
                String.class,
                "Melissa officinalis");
        assertThat(fotoUrl).startsWith("/api/plantas/fotos/").endsWith(".jpg");

        mockMvc.perform(get(fotoUrl).with(anonymous()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imagen));
    }
}
