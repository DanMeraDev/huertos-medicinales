package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.config.JwtService;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.ActividadRevisionResponse;
import com.huertos.dto.ParticipanteRevisionResponse;
import com.huertos.dto.ResultadoVerificacionResponse;
import com.huertos.dto.VerificarActividadMspRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.FaseLunar;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.MspRevisionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MspRevisionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MspRevisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private MspRevisionService mspRevisionService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void listarPendientesRetorna200ConLaBandejaDeAuditoria() throws Exception {
        ActividadRevisionResponse actividad = new ActividadRevisionResponse(
                21L,
                3L,
                "Huerto Comunitario Allpa Viva",
                "Ritual de siembra",
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Ritual de siembra con chicha de jora",
                "actividades/3f2a9b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c.jpg",
                null,
                1,
                EstadoVerificacion.PENDIENTE,
                List.of(new ParticipanteRevisionResponse(7L, "María Tránsito Amaguaña Alba", 4))
        );

        when(mspRevisionService.listarPendientes()).thenReturn(List.of(actividad));

        mockMvc.perform(get("/api/msp/actividades-ancestrales/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(21))
                .andExpect(jsonPath("$[0].nombreComunidad").value("Huerto Comunitario Allpa Viva"))
                .andExpect(jsonPath("$[0].tituloSaber").value("Ritual de siembra"))
                .andExpect(jsonPath("$[0].fechaRealizacion").value("2026-11-10"))
                .andExpect(jsonPath("$[0].numeroIntento").value(1))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].participantes[0].nombreCompleto").value("María Tránsito Amaguaña Alba"))
                .andExpect(jsonPath("$[0].participantes[0].horasTrabajadas").value(4));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void listarPendientesRetorna403ParaRolNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/msp/actividades-ancestrales/pendientes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void verificarActividadRetorna200ConElResultadoDelDictamen() throws Exception {
        ResultadoVerificacionResponse respuesta = new ResultadoVerificacionResponse(
                21L,
                EstadoVerificacion.APROBADA,
                20,
                LocalDateTime.of(2026, 11, 12, 15, 0)
        );

        when(mspRevisionService.dictaminarActividad(eq("tecnico@msp.gob.ec"), eq(21L),
                any(VerificarActividadMspRequest.class)))
                .thenReturn(respuesta);

        mockMvc.perform(post("/api/msp/actividades-ancestrales/21/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerificarActividadMspRequest(
                                EstadoVerificacion.APROBADA, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actividadId").value(21))
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.puntosComunidadAcreditados").value(20))
                .andExpect(jsonPath("$.fechaVerificacion").value("2026-11-12T15:00:00"));
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void verificarActividadRetorna400CuandoFaltaElDictamen() throws Exception {
        mockMvc.perform(post("/api/msp/actividades-ancestrales/21/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observacion\":\"Sin dictamen\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nuevoEstado").value("El dictamen de la actividad es obligatorio"));
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void verificarActividadRetorna409CuandoLaActividadYaFueDictaminada() throws Exception {
        when(mspRevisionService.dictaminarActividad(eq("tecnico@msp.gob.ec"), eq(21L),
                any(VerificarActividadMspRequest.class)))
                .thenThrow(new ConflictoException(
                        "La actividad ancestral ya fue dictaminada y no se encuentra en estado pendiente"));

        mockMvc.perform(post("/api/msp/actividades-ancestrales/21/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerificarActividadMspRequest(
                                EstadoVerificacion.APROBADA, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "La actividad ancestral ya fue dictaminada y no se encuentra en estado pendiente"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void verificarActividadRetorna403ParaRolNoAutorizado() throws Exception {
        mockMvc.perform(post("/api/msp/actividades-ancestrales/21/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerificarActividadMspRequest(
                                EstadoVerificacion.APROBADA, null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void verificarActividadRetorna403CuandoNoEstaAutenticado() throws Exception {
        mockMvc.perform(post("/api/msp/actividades-ancestrales/21/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerificarActividadMspRequest(
                                EstadoVerificacion.APROBADA, null))))
                .andExpect(status().isForbidden());
    }
}
