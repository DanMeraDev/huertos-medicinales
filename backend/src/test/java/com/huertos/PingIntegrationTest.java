package com.huertos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Recorre Controller, Service, Repository y una MariaDB real levantada con Testcontainers.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingConsultaLaBaseDeDatosReal() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ok"))
                .andExpect(jsonPath("$.baseDeDatos").value(org.hamcrest.Matchers.containsString("MariaDB")));
    }
}
