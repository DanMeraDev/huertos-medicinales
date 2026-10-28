package com.huertos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.PingResponse;
import com.huertos.service.PingService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PingController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173")
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PingService pingService;

    @Test
    void pingEsPublicoYDevuelve200() throws Exception {
        when(pingService.ping()).thenReturn(new PingResponse("ok", "11.4.8-MariaDB", Instant.EPOCH));

        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ok"))
                .andExpect(jsonPath("$.baseDeDatos").value("11.4.8-MariaDB"));
    }

    @Test
    void rutaNoPublicaSinAutenticacionDevuelve403() throws Exception {
        mockMvc.perform(get("/api/comunidades"))
                .andExpect(status().isForbidden());
    }
}
