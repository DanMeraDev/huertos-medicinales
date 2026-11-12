package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.config.JwtService;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.ActividadAncestralResponse;
import com.huertos.dto.ParticipanteActividadDto;
import com.huertos.dto.RegistrarActividadAncestralRequest;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.model.EstadoVerificacion;
import com.huertos.model.FaseLunar;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.ActividadAncestralService;
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

@WebMvcTest(ActividadAncestralController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ActividadAncestralControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ActividadAncestralService actividadAncestralService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private RegistrarActividadAncestralRequest requestValido() {
        return new RegistrarActividadAncestralRequest(
                1L,
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Ritual de siembra con chicha de jora y sahumerio",
                "https://evidencias.gob.ec/foto-ritual.jpg",
                null,
                List.of(new ParticipanteActividadDto(7L, 4))
        );
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarActividadGrandeRetorna201ConActividadPendiente() throws Exception {
        RegistrarActividadAncestralRequest request = requestValido();
        ActividadAncestralResponse response = new ActividadAncestralResponse(
                21L,
                3L,
                "Ritual de siembra",
                EstadoVerificacion.PENDIENTE,
                1,
                LocalDate.of(2026, 11, 10),
                List.of(new ParticipanteActividadDto(7L, 4))
        );

        when(actividadAncestralService.registrarActividadGrande(eq("responsable@huertos.gob.ec"), any(RegistrarActividadAncestralRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/actividades-ancestrales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(21))
                .andExpect(jsonPath("$.comunidadId").value(3))
                .andExpect(jsonPath("$.tituloSaber").value("Ritual de siembra"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.numeroIntento").value(1))
                .andExpect(jsonPath("$.fechaRealizacion").value("2026-11-10"))
                .andExpect(jsonPath("$.participantes[0].miembroId").value(7))
                .andExpect(jsonPath("$.participantes[0].horasTrabajadas").value(4));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarActividadGrandeRetorna400CuandoParticipantesEstaVacio() throws Exception {
        RegistrarActividadAncestralRequest request = new RegistrarActividadAncestralRequest(
                1L,
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Ritual de siembra con chicha de jora y sahumerio",
                "https://evidencias.gob.ec/foto-ritual.jpg",
                null,
                List.of()
        );

        mockMvc.perform(post("/api/actividades-ancestrales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.participantes")
                        .value("Debe registrar al menos un miembro participante en la actividad ancestral"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarActividadGrandeRetorna400CuandoFaltaFotografiaObligatoria() throws Exception {
        RegistrarActividadAncestralRequest request = new RegistrarActividadAncestralRequest(
                1L,
                LocalDate.of(2026, 11, 10),
                FaseLunar.PURA_KILLA_LLENA,
                "Ritual de siembra con chicha de jora y sahumerio",
                "",
                null,
                List.of(new ParticipanteActividadDto(7L, 4))
        );

        mockMvc.perform(post("/api/actividades-ancestrales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.evidenciaFotoUrl")
                        .value("La fotografía de evidencia es obligatoria para la revisión del MSP"));
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void registrarActividadGrandeRetorna403ParaRolNoAutorizado() throws Exception {
        mockMvc.perform(post("/api/actividades-ancestrales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarActividadGrandeRetorna403CuandoNoEstaAutenticado() throws Exception {
        mockMvc.perform(post("/api/actividades-ancestrales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestValido())))
                .andExpect(status().isForbidden());
    }
}
