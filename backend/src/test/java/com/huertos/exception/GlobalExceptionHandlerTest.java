package com.huertos.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void reglaNegocioDevuelve400ConElMensajeEnDetail() throws Exception {
        mockMvc.perform(get("/regla-incumplida"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La superficie excede el área del huerto"));
    }

    @Test
    void accesoDenegadoDevuelve403ConElMensajeEnDetail() throws Exception {
        mockMvc.perform(get("/acceso-denegado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("No tiene permisos para consultar el inventario de otra comunidad"));
    }

    @Test
    void formatoDeImagenNoAdmitidoDevuelve415() throws Exception {
        mockMvc.perform(get("/formato-no-admitido"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.detail").value("Formato no admitido. Seleccione una imagen JPG, PNG o WEBP"));
    }

    @Test
    void imagenDemasiadoGrandeDevuelve413() throws Exception {
        mockMvc.perform(get("/imagen-grande"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.detail").value("La imagen supera el límite de 5 MB. Intente reducir la resolución"));
    }

    @Test
    void subidaQueSuperaElLimiteDeSpringDevuelve413ConElMismoMensaje() throws Exception {
        mockMvc.perform(get("/limite-de-subida"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.detail").value("La imagen supera el límite de 5 MB. Intente reducir la resolución"));
    }

    @RestController
    static class ControladorDePrueba {

        @GetMapping("/formato-no-admitido")
        void rechazarFormato() {
            throw new FormatoImagenNoAdmitidoException();
        }

        @GetMapping("/imagen-grande")
        void rechazarImagenGrande() {
            throw new ImagenDemasiadoGrandeException();
        }

        @GetMapping("/limite-de-subida")
        void superarLimiteDeSubida() {
            throw new MaxUploadSizeExceededException(5 * 1024 * 1024);
        }

        @GetMapping("/regla-incumplida")
        void incumplirRegla() {
            throw new ReglaNegocioException("La superficie excede el área del huerto");
        }

        @GetMapping("/acceso-denegado")
        void denegarAcceso() {
            throw new AccessDeniedException("No tiene permisos para consultar el inventario de otra comunidad");
        }
    }
}
