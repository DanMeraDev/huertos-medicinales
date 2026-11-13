package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.RegistrarSiembraRequest;
import com.huertos.dto.SiembraDetalleResponse;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.CualidadTermica;
import com.huertos.model.EstadoCultivo;
import com.huertos.model.FaseLunar;
import com.huertos.service.SiembraService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SiembraController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SiembraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private SiembraService siembraService;

    @Test
    @WithMockUser(username = "resp@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarSiembra_retorna201CreatedParaResponsableComunidad() throws Exception {
        var request = new RegistrarSiembraRequest(
                10L,
                25L,
                new BigDecimal("4.50"),
                LocalDate.now(),
                FaseLunar.WINAK_KILLA_CRECIENTE,
                false
        );

        var response = new SiembraDetalleResponse(
                1L,
                10L,
                "Zona Andina",
                25L,
                55L,
                "Manzanilla",
                CualidadTermica.CALIENTE,
                new BigDecimal("4.50"),
                request.fechaSiembra(),
                FaseLunar.WINAK_KILLA_CRECIENTE,
                false,
                EstadoCultivo.SEMBRADO
        );

        when(siembraService.registrarSiembra(eq("resp@huertos.gob.ec"), any(RegistrarSiembraRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comunidad/siembras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.subhuertoId").value(10L))
                .andExpect(jsonPath("$.nombreSubhuerto").value("Zona Andina"))
                .andExpect(jsonPath("$.bodegaMaterialId").value(25L))
                .andExpect(jsonPath("$.plantaId").value(55L))
                .andExpect(jsonPath("$.nombreComunPlanta").value("Manzanilla"))
                .andExpect(jsonPath("$.cualidadTermica").value("CALIENTE"))
                .andExpect(jsonPath("$.cantidadSembrada").value(4.50))
                .andExpect(jsonPath("$.tuvoAlertaAltitud").value(false))
                .andExpect(jsonPath("$.estadoCultivo").value("SEMBRADO"));
    }

    @Test
    @WithMockUser(username = "resp@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarSiembra_retorna400BadRequestCuandoCantidadEsNegativa() throws Exception {
        var request = new RegistrarSiembraRequest(
                10L,
                25L,
                new BigDecimal("-2.00"),
                LocalDate.now(),
                FaseLunar.WINAK_KILLA_CRECIENTE,
                false
        );

        mockMvc.perform(post("/api/comunidad/siembras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "resp@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarSiembra_retorna400BadRequestCuandoStockInsuficiente() throws Exception {
        var request = new RegistrarSiembraRequest(
                10L,
                25L,
                new BigDecimal("20.00"),
                LocalDate.now(),
                FaseLunar.WINAK_KILLA_CRECIENTE,
                false
        );

        when(siembraService.registrarSiembra(eq("resp@huertos.gob.ec"), any(RegistrarSiembraRequest.class)))
                .thenThrow(new ReglaNegocioException("Stock insuficiente en bodega"));

        mockMvc.perform(post("/api/comunidad/siembras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Stock insuficiente en bodega"));
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void registrarSiembra_retorna403ForbiddenParaTecnicoMSP() throws Exception {
        var request = new RegistrarSiembraRequest(
                10L,
                25L,
                new BigDecimal("2.00"),
                LocalDate.now(),
                FaseLunar.PURA_KILLA_LLENA,
                false
        );

        mockMvc.perform(post("/api/comunidad/siembras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarSiembra_retorna403ForbiddenParaAnonimo() throws Exception {
        var request = new RegistrarSiembraRequest(
                10L,
                25L,
                new BigDecimal("2.00"),
                LocalDate.now(),
                FaseLunar.PURA_KILLA_LLENA,
                false
        );

        mockMvc.perform(post("/api/comunidad/siembras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "resp@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarSiembra_aceptaElCuerpoSinConfirmacionDeAltitud() throws Exception {
        when(siembraService.registrarSiembra(eq("resp@huertos.gob.ec"), any(RegistrarSiembraRequest.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/comunidad/siembras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subhuertoId":10,"bodegaMaterialId":25,"cantidadSembrada":4.50,
                                 "fechaSiembra":"2026-11-10","faseLunarSiembra":"WINAK_KILLA_CRECIENTE"}
                                """))
                .andExpect(status().isCreated());
    }
}
