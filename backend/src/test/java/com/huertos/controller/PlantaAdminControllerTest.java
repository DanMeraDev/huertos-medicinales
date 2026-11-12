package com.huertos.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import com.huertos.service.PlantaAdminService;
import com.huertos.service.impl.AlmacenamientoServiceImpl;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlantaAdminController.class)
@Import({SecurityConfig.class, AlmacenamientoServiceImpl.class})
class PlantaAdminControllerTest {

    private static final String RUTA = "/api/msp/plantas/fotografia";
    private static final String RUTA_PLANTAS = "/api/msp/plantas";
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};

    @TempDir
    static Path directorioFotos;

    @DynamicPropertySource
    static void propiedades(DynamicPropertyRegistry registro) {
        registro.add("app.fotos.directorio", () -> directorioFotos.toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlantaAdminService plantaAdminService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Test
    @WithMockUser(username = "tecnico.msp@huertos.gob.ec", roles = "TECNICO_MSP")
    void registraLaPlantaCon201YLaUbicacionDeSuFichaPublica() throws Exception {
        when(plantaAdminService.registrarPlanta(any(CrearPlantaRequest.class))).thenReturn(fichaRegistrada());

        mockMvc.perform(post(RUTA_PLANTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/publico/plantas/50"))
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.nombreComun").value("Hierba de prueba"))
                .andExpect(jsonPath("$.nombreCientifico").value("Herba ficticia"))
                .andExpect(jsonPath("$.cualidadTermica").value("FRIA"))
                .andExpect(jsonPath("$.categorias", contains("SEDANTE")))
                .andExpect(jsonPath("$.fotografiaUrl").value("plantas/foto.jpg"));
    }

    @Test
    void rechazaElRegistroSinToken() throws Exception {
        mockMvc.perform(post(RUTA_PLANTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isForbidden());
        verifyNoInteractions(plantaAdminService);
    }

    @Test
    void niegaElRegistroAlResponsableDeComunidad() throws Exception {
        mockMvc.perform(post(RUTA_PLANTAS)
                        .with(user("rosa@huertos.gob.ec").roles("RESPONSABLE_COMUNIDAD"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isForbidden());
        verifyNoInteractions(plantaAdminService);
    }

    @Test
    void niegaElRegistroAlTecnicoSis() throws Exception {
        mockMvc.perform(post(RUTA_PLANTAS)
                        .with(user("tecnico.sis@huertos.gob.ec").roles("TECNICO_SIS"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isForbidden());
        verifyNoInteractions(plantaAdminService);
    }

    @Test
    @WithMockUser(roles = "TECNICO_MSP")
    void rechazaLosCamposObligatoriosInvalidosCon400PorCampo() throws Exception {
        CrearPlantaRequest invalida = new CrearPlantaRequest("", null, "Herba ficticia", "Ficticiaceae", null,
                null, null, null, null, null, "Alivia el dolor", null, null, Set.of(), null);

        mockMvc.perform(post(RUTA_PLANTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreComun").exists())
                .andExpect(jsonPath("$.errores.categorias").exists())
                .andExpect(jsonPath("$.errores.cualidadTermica").exists());
        verifyNoInteractions(plantaAdminService);
    }

    @Test
    @WithMockUser(roles = "TECNICO_MSP")
    void rechazaUnNombreCientificoDuplicadoCon409() throws Exception {
        when(plantaAdminService.registrarPlanta(any(CrearPlantaRequest.class)))
                .thenThrow(new ConflictoException("El nombre científico ya existe en el catálogo"));

        mockMvc.perform(post(RUTA_PLANTAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El nombre científico ya existe en el catálogo"));
    }

    private static MockMultipartFile archivo(byte[] contenido) {
        return new MockMultipartFile("archivo", "manzanilla.jpg", "image/jpeg", contenido);
    }

    private static CrearPlantaRequest solicitudValida() {
        return new CrearPlantaRequest("Hierba de prueba", null, "Herba ficticia", "Ficticiaceae",
                CualidadTermica.FRIA, CicloCultivo.CORTO, OrigenPlanta.NATIVA, 2000, 3200, "Costa, Sierra",
                "Alivia el dolor", null, null, Set.of(CategoriaUso.SEDANTE), "plantas/foto.jpg");
    }

    private static PlantaDetalleResponse fichaRegistrada() {
        return new PlantaDetalleResponse(50L, "Hierba de prueba", null, "Herba ficticia", "Ficticiaceae",
                CualidadTermica.FRIA, CicloCultivo.CORTO, OrigenPlanta.NATIVA, 2000, 3200, "Costa, Sierra",
                "Alivia el dolor", null, null, Set.of(CategoriaUso.SEDANTE), "plantas/foto.jpg");
    }
}
