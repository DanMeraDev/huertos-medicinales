package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.service.AlmacenamientoFotoService;
import com.huertos.service.PlantaMedicinalService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(PlantaMedicinalController.class)
@Import(SecurityConfig.class)
class PlantaMedicinalControllerTest {

    private static final String MENSAJE_FOTO = "Adjunta una fotografía JPG o PNG de máximo 2 MB";
    private static final String MENSAJE_DUPLICADO = "Ya existe una planta registrada con ese nombre científico";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlantaMedicinalService plantaMedicinalService;

    @MockitoBean
    private AlmacenamientoFotoService almacenamientoFotoService;

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void registraLaPlantaYDevuelve201ConLocation() throws Exception {
        when(plantaMedicinalService.registrar(any(CrearPlantaRequest.class), any(MultipartFile.class)))
                .thenReturn(plantaCreada());

        mockMvc.perform(peticion(datosValidos()).file(fotoJpg()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/plantas/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nombreCientifico").value("Matricaria chamomilla"))
                .andExpect(jsonPath("$.cualidadTermica").value("CALIENTE"))
                .andExpect(jsonPath("$.fotoUrl").value("/api/plantas/fotos/abc.jpg"));
    }

    @Test
    @WithMockUser(roles = "ADMIN_MSP")
    void elAdministradorTambienPuedeRegistrar() throws Exception {
        when(plantaMedicinalService.registrar(any(CrearPlantaRequest.class), any(MultipartFile.class)))
                .thenReturn(plantaCreada());

        mockMvc.perform(peticion(datosValidos()).file(fotoJpg()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void sinCualidadTermicaDevuelve400ConElErrorDelCampo() throws Exception {
        Map<String, Object> datos = datosValidos();
        datos.remove("cualidadTermica");

        mockMvc.perform(peticion(datos).file(fotoJpg()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cualidadTermica").value("Este campo es obligatorio"));
        verifyNoInteractions(plantaMedicinalService);
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void sinRegionDevuelve400ConElErrorDelCampo() throws Exception {
        Map<String, Object> datos = datosValidos();
        datos.put("regionDistribucion", "");

        mockMvc.perform(peticion(datos).file(fotoJpg()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.regionDistribucion").value("Este campo es obligatorio"));
        verifyNoInteractions(plantaMedicinalService);
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void nombreCientificoDeMasDe120CaracteresDevuelve400() throws Exception {
        Map<String, Object> datos = datosValidos();
        datos.put("nombreCientifico", "a".repeat(121));

        mockMvc.perform(peticion(datos).file(fotoJpg()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreCientifico").value("Máximo 120 caracteres"));
        verifyNoInteractions(plantaMedicinalService);
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void sinLaParteFotoLlamaAlServicioConFotoNula() throws Exception {
        when(plantaMedicinalService.registrar(any(CrearPlantaRequest.class), isNull())).thenReturn(plantaCreada());

        mockMvc.perform(peticion(datosValidos()))
                .andExpect(status().isCreated());
        verify(plantaMedicinalService).registrar(any(CrearPlantaRequest.class), isNull());
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void fotoInvalidaDevuelve400ConElMensajeEnDetail() throws Exception {
        when(plantaMedicinalService.registrar(any(CrearPlantaRequest.class), any()))
                .thenThrow(new ReglaNegocioException(MENSAJE_FOTO));

        mockMvc.perform(peticion(datosValidos()).file(fotoJpg()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(MENSAJE_FOTO));
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void archivoMayorAlLimiteDeSubidaDevuelve400ConElMensajeEnDetail() throws Exception {
        when(plantaMedicinalService.registrar(any(CrearPlantaRequest.class), any()))
                .thenThrow(new MaxUploadSizeExceededException(3L * 1024 * 1024));

        mockMvc.perform(peticion(datosValidos()).file(fotoJpg()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(MENSAJE_FOTO));
    }

    @Test
    @WithMockUser(roles = "RESPONSABLE_COMUNIDAD")
    void plantaDuplicadaDevuelve409() throws Exception {
        when(plantaMedicinalService.registrar(any(CrearPlantaRequest.class), any()))
                .thenThrow(new ConflictoException(MENSAJE_DUPLICADO));

        mockMvc.perform(peticion(datosValidos()).file(fotoJpg()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(MENSAJE_DUPLICADO));
    }

    @Test
    @WithMockUser(roles = "MIEMBRO")
    void unMiembroRecibe403() throws Exception {
        mockMvc.perform(peticion(datosValidos()).file(fotoJpg()))
                .andExpect(status().isForbidden());
        verify(plantaMedicinalService, never()).registrar(any(), any());
    }

    private static MockMultipartHttpServletRequestBuilder peticion(Map<String, Object> datos) {
        MockMultipartFile parteDatos = new MockMultipartFile(
                "datos", "", MediaType.APPLICATION_JSON_VALUE, JSON.writeValueAsBytes(datos));
        return multipart("/api/plantas").file(parteDatos);
    }

    private static MockMultipartFile fotoJpg() {
        return new MockMultipartFile("foto", "manzanilla.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});
    }

    private static Map<String, Object> datosValidos() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("nombreComun", "Manzanilla");
        datos.put("nombreCientifico", "Matricaria chamomilla");
        datos.put("nombreKichwa", "Manzanilla panga");
        datos.put("familiaBotanica", "Asteraceae");
        datos.put("regionDistribucion", "Sierra: Pichincha, Imbabura");
        datos.put("altitudMsnm", "2000 a 3000");
        datos.put("cicloCultivo", "CORTO");
        datos.put("cualidadTermica", "CALIENTE");
        datos.put("usosMedicinales", "Digestivo, calmante");
        datos.put("indicacionesDosificacion", "Una taza de infusión tres veces al día");
        datos.put("contraindicaciones", "No usar en embarazo");
        return datos;
    }

    private static PlantaResponse plantaCreada() {
        return new PlantaResponse(
                7L,
                "Manzanilla",
                "Matricaria chamomilla",
                "Manzanilla panga",
                "Asteraceae",
                "Sierra: Pichincha, Imbabura",
                "2000 a 3000",
                CicloCultivo.CORTO,
                CualidadTermica.CALIENTE,
                "Digestivo, calmante",
                "Una taza de infusión tres veces al día",
                "No usar en embarazo",
                "/api/plantas/fotos/abc.jpg");
    }
}
