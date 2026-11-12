package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.ActualizarHuertoRequest;
import com.huertos.dto.HuertoDetalleResponse;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.FuenteAgua;
import com.huertos.model.TipoAbono;
import com.huertos.model.TipoCerca;
import com.huertos.model.TipoHuerto;
import com.huertos.model.TipoSuelo;
import com.huertos.service.HuertoService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HuertoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class HuertoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private HuertoService huertoService;

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void obtenerHuertoRetorna200ConDetalle() throws Exception {
        when(huertoService.obtenerPorCorreo("responsable@huertos.gob.ec"))
                .thenReturn(detalleResponse());

        mockMvc.perform(get("/api/comunidad/huerto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreHuerto").value("Huerto Allpa Viva"))
                .andExpect(jsonPath("$.areaTotalM2").value(150.00));
    }

    @Test
    @WithMockUser(username = "tecnico@huertos.gob.ec", roles = "TECNICO_MSP")
    void obtenerHuertoConRolTecnicoRetorna200() throws Exception {
        when(huertoService.obtenerPorCorreo("tecnico@huertos.gob.ec"))
                .thenReturn(detalleResponse());

        mockMvc.perform(get("/api/comunidad/huerto"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerHuertoSinAutenticacionRetorna403() throws Exception {
        mockMvc.perform(get("/api/comunidad/huerto"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "miembro@huertos.gob.ec", roles = "VOLUNTARIO")
    void obtenerHuertoConRolDiferenteRetorna403() throws Exception {
        mockMvc.perform(get("/api/comunidad/huerto"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void actualizarHuertoConPayloadValidoRetorna200() throws Exception {
        ActualizarHuertoRequest request = requestValido();

        when(huertoService.actualizarPorCorreo(eq("responsable@huertos.gob.ec"), any(ActualizarHuertoRequest.class)))
                .thenReturn(detalleResponse());

        mockMvc.perform(put("/api/comunidad/huerto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.areaTotalM2").value(150.00));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void actualizarHuertoSinAreaRetorna400() throws Exception {
        ActualizarHuertoRequest requestSinArea = new ActualizarHuertoRequest(
                new BigDecimal("-0.1234567"),
                new BigDecimal("-78.5678901"),
                2500,
                null,
                null,
                DisenoPrincipal.EN_TIERRA,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/comunidad/huerto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSinArea)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void actualizarHuertoSinLatitudRetorna400() throws Exception {
        ActualizarHuertoRequest requestSinLatitud = new ActualizarHuertoRequest(
                null,
                new BigDecimal("-78.5678901"),
                2500,
                null,
                new BigDecimal("150.00"),
                DisenoPrincipal.EN_TIERRA,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/comunidad/huerto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSinLatitud)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void actualizarHuertoSinDisenoPrincipalRetorna400() throws Exception {
        ActualizarHuertoRequest requestSinDiseno = new ActualizarHuertoRequest(
                new BigDecimal("-0.1234567"),
                new BigDecimal("-78.5678901"),
                2500,
                null,
                new BigDecimal("150.00"),
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/comunidad/huerto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestSinDiseno)))
                .andExpect(status().isBadRequest());
    }

    private HuertoDetalleResponse detalleResponse() {
        return new HuertoDetalleResponse(
                1L,
                3L,
                "Huerto Allpa Viva",
                new BigDecimal("-0.1234567"),
                new BigDecimal("-78.5678901"),
                2500,
                "NORTE_SUR",
                new BigDecimal("150.00"),
                DisenoPrincipal.EN_TIERRA,
                TipoSuelo.ARCILLOSO,
                FuenteAgua.GOTEO,
                TipoAbono.COMPOST,
                TipoCerca.CERCA_VIVA,
                "Muro de cabuya",
                TipoHuerto.BASICO,
                0,
                BigDecimal.ZERO
        );
    }

    private ActualizarHuertoRequest requestValido() {
        return new ActualizarHuertoRequest(
                new BigDecimal("-0.1234567"),
                new BigDecimal("-78.5678901"),
                2500,
                "NORTE_SUR",
                new BigDecimal("150.00"),
                DisenoPrincipal.EN_TIERRA,
                TipoSuelo.ARCILLOSO,
                FuenteAgua.GOTEO,
                TipoAbono.COMPOST,
                TipoCerca.CERCA_VIVA,
                "Muro de cabuya"
        );
    }
}

