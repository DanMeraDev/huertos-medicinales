package com.huertos.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.controller.PingController;
import com.huertos.service.PingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PingController.class)
@Import(SecurityConfig.class)
class SeguridadJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private PingService pingService;

    @Test
    void conTokenValidoPasaLaSeguridad() throws Exception {
        String token = jwtService.generarToken("rosa@correo.com", "MIEMBRO");

        mockMvc.perform(get("/api/ruta-inexistente").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void conTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/ruta-inexistente").header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/ruta-inexistente"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void elRegistroYElLoginSonPublicos() throws Exception {
        mockMvc.perform(get("/api/auth/login")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/usuarios/registro")).andExpect(status().isNotFound());
    }
}
