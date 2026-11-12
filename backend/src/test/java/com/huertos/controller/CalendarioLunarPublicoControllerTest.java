package com.huertos.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.CalendarioLunarResponse;
import com.huertos.model.FaseLunar;
import com.huertos.service.CalendarioLunarService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CalendarioLunarPublicoController.class)
@Import({SecurityConfig.class, CalendarioLunarPublicoControllerTest.RelojFijo.class})
class CalendarioLunarPublicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalendarioLunarService calendarioLunarService;

    @Test
    void consultaLaFaseActualSinTokenYDevuelve200() throws Exception {
        when(calendarioLunarService.obtenerFaseActual(any())).thenReturn(respuesta());

        mockMvc.perform(get("/api/publico/calendario-lunar/actual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faseLunar").value("PURA_KILLA_LLENA"))
                .andExpect(jsonPath("$.nombreKichwa").value("Pura Killa"))
                .andExpect(jsonPath("$.nombreEspanol").value("Luna Llena"))
                .andExpect(jsonPath("$.fechaInicio").value("2026-11-01"))
                .andExpect(jsonPath("$.fechaFin").value("2026-11-07"));
    }

    @Test
    void consultaLaFaseVigentePorLaFechaDelReloj() throws Exception {
        when(calendarioLunarService.obtenerFaseActual(any())).thenReturn(respuesta());

        mockMvc.perform(get("/api/publico/calendario-lunar/actual"))
                .andExpect(status().isOk());

        ArgumentCaptor<LocalDate> fechaConsultada = ArgumentCaptor.forClass(LocalDate.class);
        verify(calendarioLunarService).obtenerFaseActual(fechaConsultada.capture());
        assertThat(fechaConsultada.getValue()).isEqualTo(LocalDate.of(2026, 11, 11));
    }

    @Test
    void consultaLaFaseDeUnaFechaIndicada() throws Exception {
        when(calendarioLunarService.obtenerFaseActual(any())).thenReturn(respuesta());

        mockMvc.perform(get("/api/publico/calendario-lunar").param("fecha", "2026-12-25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faseLunar").value("PURA_KILLA_LLENA"));

        verify(calendarioLunarService).obtenerFaseActual(LocalDate.of(2026, 12, 25));
    }

    private CalendarioLunarResponse respuesta() {
        return new CalendarioLunarResponse(
                3L, FaseLunar.PURA_KILLA_LLENA, "Pura Killa", "Luna Llena",
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7),
                "Cosecha de raíces y hojas medicinales.", "Kinti habla en la noche.");
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RelojFijo {

        @Bean
        @Primary
        Clock reloj() {
            return Clock.fixed(Instant.parse("2026-11-11T12:00:00Z"), ZoneOffset.UTC);
        }
    }
}
