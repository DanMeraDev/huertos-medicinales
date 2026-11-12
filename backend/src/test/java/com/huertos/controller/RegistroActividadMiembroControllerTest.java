package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.RegistrarLaborMiembroRequest;
import com.huertos.dto.ResultadoAcreditacionMiembroResponse;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.NivelMiembro;
import com.huertos.service.RegistroActividadMiembroService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegistroActividadMiembroController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class RegistroActividadMiembroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private RegistroActividadMiembroService registroActividadMiembroService;

    @Test
    @WithMockUser(username = "resp@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarLabor_retorna201Created_paraResponsableComunidad() throws Exception {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                4,
                "Jornada de deshierbe"
        );

        var response = new ResultadoAcreditacionMiembroResponse(
                1L,
                25L,
                4,
                24,
                NivelMiembro.APRENDIZ,
                false,
                List.of()
        );

        when(registroActividadMiembroService.registrarLabor(eq("resp@huertos.gob.ec"), any(RegistrarLaborMiembroRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comunidad/actividades-miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registroId").value(1L))
                .andExpect(jsonPath("$.miembroId").value(25L))
                .andExpect(jsonPath("$.puntosOtorgados").value(4))
                .andExpect(jsonPath("$.puntosTotales").value(24))
                .andExpect(jsonPath("$.nivel").value("APRENDIZ"))
                .andExpect(jsonPath("$.subioDeNivel").value(false));
    }

    @Test
    @WithMockUser(username = "resp@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarLabor_retorna400BadRequest_cuandoHorasSuperan24() throws Exception {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                25,
                null
        );

        mockMvc.perform(post("/api/comunidad/actividades-miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "resp@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarLabor_retorna400BadRequest_cuandoFechaEsFutura() throws Exception {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now().plusDays(2),
                4,
                null
        );

        mockMvc.perform(post("/api/comunidad/actividades-miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "resp@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarLabor_retorna400BadRequest_cuandoReglaNegocioFalla() throws Exception {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                4,
                null
        );

        when(registroActividadMiembroService.registrarLabor(eq("resp@huertos.gob.ec"), any(RegistrarLaborMiembroRequest.class)))
                .thenThrow(new ReglaNegocioException("El miembro seleccionado no está activo en la comunidad"));

        mockMvc.perform(post("/api/comunidad/actividades-miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El miembro seleccionado no está activo en la comunidad"));
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void registrarLabor_retorna403Forbidden_paraTecnicoMsp() throws Exception {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                4,
                null
        );

        mockMvc.perform(post("/api/comunidad/actividades-miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarLabor_retorna403Forbidden_paraAnonimo() throws Exception {
        var request = new RegistrarLaborMiembroRequest(
                25L,
                5L,
                LocalDate.now(),
                4,
                null
        );

        mockMvc.perform(post("/api/comunidad/actividades-miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
