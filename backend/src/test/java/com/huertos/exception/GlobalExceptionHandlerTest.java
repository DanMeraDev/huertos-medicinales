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

    @RestController
    static class ControladorDePrueba {

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
