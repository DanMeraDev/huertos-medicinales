package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.repository.UsuarioRepository;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.CrearSubhuertoRequest;
import com.huertos.dto.SubhuertoResponse;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.SubtipoSubhuerto;
import com.huertos.service.SubhuertoService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SubhuertoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SubhuertoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private SubhuertoService subhuertoService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "resp@comunidad.ec", roles = "RESPONSABLE_COMUNIDAD")
    void crearSubhuerto_retorna201CreatedParaResponsableComunidad() throws Exception {
        var request = new CrearSubhuertoRequest(
                "Macetas Medicinales",
                SubtipoSubhuerto.MACETAS_PISO,
                new BigDecimal("12.50")
        );
        var response = new SubhuertoResponse(
                7L,
                20L,
                "Macetas Medicinales",
                SubtipoSubhuerto.MACETAS_PISO,
                new BigDecimal("12.50"),
                true
        );

        when(subhuertoService.crear(eq("resp@comunidad.ec"), any(CrearSubhuertoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comunidad/subhuertos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.huertoId").value(20L))
                .andExpect(jsonPath("$.nombreZona").value("Macetas Medicinales"))
                .andExpect(jsonPath("$.subtipo").value("MACETAS_PISO"))
                .andExpect(jsonPath("$.areaM2").value(12.50))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(username = "resp@comunidad.ec", roles = "RESPONSABLE_COMUNIDAD")
    void crearSubhuerto_retorna400CuandoSuperaTresSubtipos() throws Exception {
        var request = new CrearSubhuertoRequest(
                "Zona Extra",
                SubtipoSubhuerto.COLGANTE_TECHO,
                new BigDecimal("8.00")
        );

        when(subhuertoService.crear(eq("resp@comunidad.ec"), any(CrearSubhuertoRequest.class)))
                .thenThrow(new ReglaNegocioException("Máximo 3 subtipos por huerto según norma MSP"));

        mockMvc.perform(post("/api/comunidad/subhuertos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "resp@comunidad.ec", roles = "RESPONSABLE_COMUNIDAD")
    void listarSubhuertos_retorna200ConLista() throws Exception {
        var item = new SubhuertoResponse(
                1L,
                20L,
                "Surcos Altos",
                SubtipoSubhuerto.SURCOS_WACHU,
                new BigDecimal("25.00"),
                true
        );
        when(subhuertoService.listarPorUsuario("resp@comunidad.ec")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/comunidad/subhuertos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreZona").value("Surcos Altos"))
                .andExpect(jsonPath("$[0].subtipo").value("SURCOS_WACHU"));
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void crearSubhuerto_retorna403ParaTecnicoMsp() throws Exception {
        var request = new CrearSubhuertoRequest(
                "Zona 1",
                SubtipoSubhuerto.SURCOS_WACHU,
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/comunidad/subhuertos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tecnico@msp.gob.ec", roles = "TECNICO_MSP")
    void listarSubhuertos_retorna403ParaTecnicoMsp() throws Exception {
        mockMvc.perform(get("/api/comunidad/subhuertos"))
                .andExpect(status().isForbidden());
    }
}
