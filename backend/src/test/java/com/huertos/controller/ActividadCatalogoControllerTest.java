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
import com.huertos.dto.ActividadCatalogoResponse;
import com.huertos.dto.CrearActividadCatalogoRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.model.CategoriaActividad;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.ActividadCatalogoService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ActividadCatalogoController.class)
@Import(SecurityConfig.class)
class ActividadCatalogoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ActividadCatalogoService actividadCatalogoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "resp@comunidad.ec", roles = "RESPONSABLE_COMUNIDAD")
    void crearActividad_retorna201CreatedParaResponsableComunidad() throws Exception {
        var request = new CrearActividadCatalogoRequest("Riego por goteo", CategoriaActividad.RIEGO);
        var response = new ActividadCatalogoResponse(12L, "Riego por goteo", CategoriaActividad.RIEGO, 1, true);

        when(actividadCatalogoService.crear(eq("resp@comunidad.ec"), any(CrearActividadCatalogoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comunidad/actividades-catalogo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12L))
                .andExpect(jsonPath("$.nombre").value("Riego por goteo"))
                .andExpect(jsonPath("$.categoria").value("RIEGO"))
                .andExpect(jsonPath("$.puntosPorHora").value(1))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    @WithMockUser(username = "resp@comunidad.ec", roles = "RESPONSABLE_COMUNIDAD")
    void crearActividad_retorna409CuandoNombreDuplicado() throws Exception {
        var request = new CrearActividadCatalogoRequest("Riego por goteo", CategoriaActividad.RIEGO);

        when(actividadCatalogoService.crear(eq("resp@comunidad.ec"), any(CrearActividadCatalogoRequest.class)))
                .thenThrow(new ConflictoException("Ya existe una actividad con este nombre en tu comunidad"));

        mockMvc.perform(post("/api/comunidad/actividades-catalogo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "resp@comunidad.ec", roles = "RESPONSABLE_COMUNIDAD")
    void listarActividades_retorna200ConCatalogo() throws Exception {
        var item = new ActividadCatalogoResponse(1L, "Aporque de manzanilla", CategoriaActividad.REPLANTE_APORQUE, 1, true);
        when(actividadCatalogoService.listarPorResponsable("resp@comunidad.ec")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/comunidad/actividades-catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Aporque de manzanilla"))
                .andExpect(jsonPath("$[0].puntosPorHora").value(1));
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void crearActividad_retorna403ParaRolNoAutorizado() throws Exception {
        var request = new CrearActividadCatalogoRequest("Riego", CategoriaActividad.RIEGO);

        mockMvc.perform(post("/api/comunidad/actividades-catalogo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}