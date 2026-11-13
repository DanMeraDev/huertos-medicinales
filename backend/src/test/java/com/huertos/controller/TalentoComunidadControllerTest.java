package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.CrearTalentoComunidadRequest;
import com.huertos.dto.TalentoComunidadResponse;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.service.TalentoComunidadService;
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

@WebMvcTest(TalentoComunidadController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class TalentoComunidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private TalentoComunidadService talentoComunidadService;

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarTalentoRetorna201ConDatosValidos() throws Exception {
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Maestro del Abonaje 1",
                "Completó 50 sesiones de preparación y distribución de compost",
                50
        );

        TalentoComunidadResponse response = new TalentoComunidadResponse(
                1L,
                3L,
                2L,
                "Abonar camas de siembra",
                "Maestro del Abonaje 1",
                "Completó 50 sesiones de preparación y distribución de compost",
                50,
                "insignia-semilla",
                true,
                LocalDateTime.of(2026, 11, 10, 12, 0)
        );

        when(talentoComunidadService.registrarTalentoPorCorreo(eq("responsable@huertos.gob.ec"), any(CrearTalentoComunidadRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comunidades/mi-comunidad/talentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.comunidadId").value(3))
                .andExpect(jsonPath("$.actividadComunidadId").value(2))
                .andExpect(jsonPath("$.nombreActividad").value("Abonar camas de siembra"))
                .andExpect(jsonPath("$.nombreInsignia").value("Maestro del Abonaje 1"))
                .andExpect(jsonPath("$.metaRepeticiones").value(50))
                .andExpect(jsonPath("$.iconoCodigo").value("insignia-semilla"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarTalentoRetorna400CuandoMetaEsMenorOIgualACero() throws Exception {
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Maestro del Abonaje 1",
                "Descripción válida",
                0
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/talentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.metaRepeticiones").value("La meta de repeticiones debe ser un número entero mayor a 0"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarTalentoRetorna400CuandoNombreTieneCaracteresEspeciales() throws Exception {
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Insignia @ # $",
                "Descripción válida",
                10
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/talentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreInsignia").value("El nombre de la insignia solo puede contener letras, números y espacios"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarTalentoRetorna409CuandoNombreEstaDuplicadoEnComunidad() throws Exception {
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Maestro del Abonaje 1",
                "Descripción válida",
                50
        );

        when(talentoComunidadService.registrarTalentoPorCorreo(eq("responsable@huertos.gob.ec"), any(CrearTalentoComunidadRequest.class)))
                .thenThrow(new ConflictoException("Ya existe un talento o medalla registrada con este nombre en tu comunidad"));

        mockMvc.perform(post("/api/comunidades/mi-comunidad/talentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe un talento o medalla registrada con este nombre en tu comunidad"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarTalentoRetorna404CuandoActividadNoExiste() throws Exception {
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                99L,
                "Maestro del Riego",
                "Descripción válida",
                20
        );

        when(talentoComunidadService.registrarTalentoPorCorreo(eq("responsable@huertos.gob.ec"), any(CrearTalentoComunidadRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("La actividad comunitaria especificada no existe o no pertenece a tu comunidad"));

        mockMvc.perform(post("/api/comunidades/mi-comunidad/talentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("La actividad comunitaria especificada no existe o no pertenece a tu comunidad"));
    }

    @Test
    @WithMockUser(username = "admin@huertos.gob.ec", roles = "ADMINISTRADOR")
    void registrarTalentoRetorna403CuandoUsuarioNoTieneRolResponsable() throws Exception {
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Insignia",
                "Descripción",
                10
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/talentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarTalentoRetorna403CuandoNoEstaAutenticado() throws Exception {
        CrearTalentoComunidadRequest request = new CrearTalentoComunidadRequest(
                2L,
                "Insignia",
                "Descripción",
                10
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/talentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void listarTalentosRetorna200ConLista() throws Exception {
        TalentoComunidadResponse talento = new TalentoComunidadResponse(
                1L,
                3L,
                2L,
                "Abonar camas de siembra",
                "Maestro del Abonaje 1",
                "Descripción",
                50,
                "insignia-semilla",
                true,
                LocalDateTime.of(2026, 11, 10, 12, 0)
        );

        when(talentoComunidadService.listarPorCorreo("responsable@huertos.gob.ec"))
                .thenReturn(List.of(talento));

        mockMvc.perform(get("/api/comunidades/mi-comunidad/talentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombreInsignia").value("Maestro del Abonaje 1"))
                .andExpect(jsonPath("$[0].metaRepeticiones").value(50));
    }

    @Test
    @WithMockUser(username = "admin@huertos.gob.ec", roles = "ADMINISTRADOR")
    void listarTalentosRetorna403CuandoUsuarioNoTieneRolResponsable() throws Exception {
        mockMvc.perform(get("/api/comunidades/mi-comunidad/talentos"))
                .andExpect(status().isForbidden());
    }
}
