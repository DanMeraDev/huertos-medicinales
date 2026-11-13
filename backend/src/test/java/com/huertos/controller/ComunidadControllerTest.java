package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.repository.UsuarioRepository;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.ActualizarComunidadRequest;
import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.model.TipoOrganizacion;
import com.huertos.service.ComunidadService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ComunidadController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ComunidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ComunidadService comunidadService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void obtenerMiComunidadRetorna200ConDetalle() throws Exception {
        ComunidadDetalleResponse response = new ComunidadDetalleResponse(
                1L,
                "Huerto San Pedro",
                TipoOrganizacion.BARRIO,
                "Pichincha",
                "Calderón",
                "Calle Central",
                "0991234567",
                "responsable@huertos.gob.ec",
                10L,
                "Juan Pérez",
                0,
                true,
                LocalDateTime.now()
        );

        when(comunidadService.obtenerPorCorreo("responsable@huertos.gob.ec")).thenReturn(response);

        mockMvc.perform(get("/api/comunidades/mi-comunidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Huerto San Pedro"))
                .andExpect(jsonPath("$.telefonoContacto").value("0991234567"))
                .andExpect(jsonPath("$.correoContacto").value("responsable@huertos.gob.ec"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void actualizarMiComunidadConPayloadValidoRetorna200() throws Exception {
        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Huerto San Pedro Renovado",
                "0997654321",
                "nuevo@huertos.gob.ec"
        );

        ComunidadDetalleResponse response = new ComunidadDetalleResponse(
                1L,
                "Huerto San Pedro Renovado",
                TipoOrganizacion.BARRIO,
                "Pichincha",
                "Calderón",
                "Calle Central",
                "0997654321",
                "nuevo@huertos.gob.ec",
                10L,
                "Juan Pérez",
                0,
                true,
                LocalDateTime.now()
        );

        when(comunidadService.actualizarPorCorreo(eq("responsable@huertos.gob.ec"), any(ActualizarComunidadRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/comunidades/mi-comunidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Huerto San Pedro Renovado"))
                .andExpect(jsonPath("$.telefonoContacto").value("0997654321"))
                .andExpect(jsonPath("$.correoContacto").value("nuevo@huertos.gob.ec"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void actualizarMiComunidadConTelefonoInvalidoRetorna400() throws Exception {
        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Huerto San Pedro",
                "1234567",
                "correo@huertos.gob.ec"
        );

        mockMvc.perform(put("/api/comunidades/mi-comunidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.telefonoContacto").exists());
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void actualizarMiComunidadConNombreInvalidoRetorna400() throws Exception {
        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Huerto @123!",
                "0991234567",
                "correo@huertos.gob.ec"
        );

        mockMvc.perform(put("/api/comunidades/mi-comunidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void actualizarMiComunidadConCorreoInvalidoRetorna400() throws Exception {
        ActualizarComunidadRequest request = new ActualizarComunidadRequest(
                "Huerto San Pedro",
                "0991234567",
                "no-es-correo"
        );

        mockMvc.perform(put("/api/comunidades/mi-comunidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correoContacto").exists());
    }

    @Test
    void peticionSinAutenticacionRetorna403() throws Exception {
        mockMvc.perform(get("/api/comunidades/mi-comunidad"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "voluntario@huertos.gob.ec", roles = "VOLUNTARIO")
    void peticionConRolDiferenteRetorna403() throws Exception {
        mockMvc.perform(get("/api/comunidades/mi-comunidad"))
                .andExpect(status().isForbidden());
    }
}
