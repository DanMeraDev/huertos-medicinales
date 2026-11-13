package com.huertos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.repository.UsuarioRepository;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.SaberAncestralDetalleResponse;
import com.huertos.dto.SaberAncestralResumenResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.model.CategoriaSaber;
import com.huertos.model.FaseLunar;
import com.huertos.service.SaberAncestralService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SaberAncestralPublicoController.class)
@Import(SecurityConfig.class)
class SaberAncestralPublicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SaberAncestralService saberAncestralService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void consultaLaRutaDeSaberesSinTokenYDevuelve200() throws Exception {
        when(saberAncestralService.listarRutaSaberes()).thenReturn(List.of(resumen()));

        mockMvc.perform(get("/api/publico/saberes-ancestrales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].ordenPaso").value(1))
                .andExpect(jsonPath("$[0].codigoHito").value("SA-PASO-1"))
                .andExpect(jsonPath("$[0].titulo").value("Preparación de la tierra"))
                .andExpect(jsonPath("$[0].tituloKichwa").value("Allpa wiñay"))
                .andExpect(jsonPath("$[0].categoria").value("PREPARACION_TIERRA"))
                .andExpect(jsonPath("$[0].faseLunarIdeal").value("YANA_KILLA_OSCURA"))
                .andExpect(jsonPath("$[0].nombreInsigniaOficial").value("Guardián de la tierra"));
    }

    @Test
    void consultaElDetalleDeUnSaberSinTokenYDevuelve200() throws Exception {
        when(saberAncestralService.obtenerPorId(1L)).thenReturn(detalle());

        mockMvc.perform(get("/api/publico/saberes-ancestrales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.horarioEnergetico").value("05:00 a 07:00 AM - Salida del sol"))
                .andExpect(jsonPath("$.contenidoEducativo").value("Prepara la tierra con abono natural."))
                .andExpect(jsonPath("$.ingredientesHerramientas").value("Chakana, chaquetaclla"))
                .andExpect(jsonPath("$.puntosBaseComunidad").value(20))
                .andExpect(jsonPath("$.nombreInsigniaOficial").value("Guardián de la tierra"));
    }

    @Test
    void avisaCon404CuandoElSaberNoExiste() throws Exception {
        when(saberAncestralService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("El saber ancestral solicitado no fue encontrado"));

        mockMvc.perform(get("/api/publico/saberes-ancestrales/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("El saber ancestral solicitado no fue encontrado"));
    }

    private SaberAncestralResumenResponse resumen() {
        return new SaberAncestralResumenResponse(
                1L, 1, "SA-PASO-1", "Preparación de la tierra", "Allpa wiñay",
                CategoriaSaber.PREPARACION_TIERRA, FaseLunar.YANA_KILLA_OSCURA,
                "Guardián de la tierra");
    }

    private SaberAncestralDetalleResponse detalle() {
        return new SaberAncestralDetalleResponse(
                1L, 1, "SA-PASO-1", "Preparación de la tierra", "Allpa wiñay",
                CategoriaSaber.PREPARACION_TIERRA, FaseLunar.YANA_KILLA_OSCURA,
                "05:00 a 07:00 AM - Salida del sol", "Prepara la tierra con abono natural.",
                "Chakana, chaquetaclla", 20, "Guardián de la tierra");
    }
}
