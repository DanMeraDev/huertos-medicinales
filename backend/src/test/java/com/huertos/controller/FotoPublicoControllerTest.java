package com.huertos.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.huertos.repository.UsuarioRepository;
import com.huertos.config.SecurityConfig;
import com.huertos.service.impl.AlmacenamientoServiceImpl;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FotoPublicoController.class)
@Import({SecurityConfig.class, AlmacenamientoServiceImpl.class})
class FotoPublicoControllerTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
    private static final byte[] WEBP = "RIFF$\u0000\u0000\u0000WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1);

    @TempDir
    static Path directorioFotos;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("app.fotos.directorio", () -> directorioFotos.toString());
    }

    @BeforeAll
    static void prepararFotos() throws IOException {
        Files.createDirectories(directorioFotos.resolve("plantas"));
        Files.createDirectories(directorioFotos.resolve("otros"));
        Files.write(directorioFotos.resolve("plantas/manzanilla.jpg"), JPEG);
        Files.write(directorioFotos.resolve("plantas/romero.webp"), WEBP);
        Files.write(directorioFotos.resolve("otros/manzanilla.jpg"), JPEG);
        Files.write(directorioFotos.resolve("secreto.jpg"), JPEG);
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void entregaUnJpegSinTokenConSusEncabezados() throws Exception {
        mockMvc.perform(get("/api/publico/fotos/plantas/manzanilla.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(JPEG))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", containsString("max-age=86400")))
                .andExpect(header().string("Cache-Control", containsString("public")));
    }

    @Test
    void entregaUnWebpConSuTipoDeContenido() throws Exception {
        mockMvc.perform(get("/api/publico/fotos/plantas/romero.webp"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(content().bytes(WEBP));
    }

    @Test
    void respondeNoEncontradoSiLaFotografiaNoExiste() throws Exception {
        mockMvc.perform(get("/api/publico/fotos/plantas/matico.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void noPermiteSalirDelDirectorioDeFotos() throws Exception {
        int estado = mockMvc.perform(get(URI.create("/api/publico/fotos/plantas/..%2Fsecreto.jpg")))
                .andReturn().getResponse().getStatus();

        assertThat(estado).isIn(400, 404);
    }

    @Test
    void respondeNoEncontradoParaSubcarpetasNoPermitidas() throws Exception {
        mockMvc.perform(get("/api/publico/fotos/otros/manzanilla.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void respondeNoEncontradoParaNombresNoValidos() throws Exception {
        mockMvc.perform(get("/api/publico/fotos/plantas/Manzanilla.JPG"))
                .andExpect(status().isNotFound());
    }
}
