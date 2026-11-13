package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.repository.UsuarioRepository;
import com.huertos.config.JwtService;
import com.huertos.config.SecurityConfig;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.service.AlmacenamientoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ActividadFotoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ActividadFotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlmacenamientoService almacenamientoService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private JwtService jwtService;

    private MockMultipartFile archivoFoto() {
        return new MockMultipartFile(
                "archivo",
                "foto.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }
        );
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void subirFotografiaRetorna200ConRutaRelativa() throws Exception {
        when(almacenamientoService.guardar(any(), eq("actividades")))
                .thenReturn("actividades/3f2a9b1c.jpg");

        mockMvc.perform(multipart("/api/actividades-ancestrales/fotografia")
                        .file(archivoFoto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotografiaUrl").value("actividades/3f2a9b1c.jpg"));
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void subirFotografiaRetorna403ParaRolNoAutorizado() throws Exception {
        mockMvc.perform(multipart("/api/actividades-ancestrales/fotografia")
                        .file(archivoFoto()))
                .andExpect(status().isForbidden());
    }

    @Test
    void subirFotografiaRetorna403CuandoNoEstaAutenticado() throws Exception {
        mockMvc.perform(multipart("/api/actividades-ancestrales/fotografia")
                        .file(archivoFoto()))
                .andExpect(status().isForbidden());
    }
}
