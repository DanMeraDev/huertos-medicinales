package com.huertos.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.service.impl.AlmacenamientoServiceImpl;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantaAdminController.class)
@Import({SecurityConfig.class, AlmacenamientoServiceImpl.class})
class PlantaAdminControllerTest {

    private static final String RUTA = "/api/msp/plantas/fotografia";
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};

    @TempDir
    static Path directorioFotos;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("app.fotos.directorio", () -> directorioFotos.toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "tecnico.msp@huertos.gob.ec", roles = "TECNICO_MSP")
    void subeLaFotografiaYDevuelveLaRutaRelativa() throws Exception {
        String respuesta = mockMvc.perform(multipart(RUTA).file(archivo(JPEG)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotografiaUrl").value(matchesPattern("plantas/[0-9a-f-]{36}\\.jpg")))
                .andReturn().getResponse().getContentAsString();

        String ruta = JsonPath.read(respuesta, "$.fotografiaUrl");
        assertThat(Files.readAllBytes(directorioFotos.resolve(ruta))).isEqualTo(JPEG);
    }

    @Test
    void rechazaLaSubidaSinToken() throws Exception {
        mockMvc.perform(multipart(RUTA).file(archivo(JPEG)))
                .andExpect(status().isForbidden());
    }

    @Test
    void niegaLaSubidaAOtrosRoles() throws Exception {
        mockMvc.perform(multipart(RUTA).file(archivo(JPEG))
                        .with(user("rosa@huertos.gob.ec").roles("RESPONSABLE_COMUNIDAD")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TECNICO_MSP")
    void rechazaUnArchivoDeTextoCon415() throws Exception {
        mockMvc.perform(multipart(RUTA).file(archivo("hola".getBytes(StandardCharsets.UTF_8))))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.detail").value("Formato no admitido. Seleccione una imagen JPG, PNG o WEBP"));
    }

    @Test
    @WithMockUser(roles = "TECNICO_MSP")
    void rechazaUnaImagenDeMasDeCincoMegasCon413() throws Exception {
        mockMvc.perform(multipart(RUTA).file(archivo(Arrays.copyOf(JPEG, 5 * 1024 * 1024 + 1))))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.detail").value("La imagen supera el límite de 5 MB. Intente reducir la resolución"));
    }

    private static MockMultipartFile archivo(byte[] contenido) {
        return new MockMultipartFile("archivo", "manzanilla.jpg", "image/jpeg", contenido);
    }
}
