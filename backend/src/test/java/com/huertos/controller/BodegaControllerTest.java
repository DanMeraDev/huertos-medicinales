package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.JwtService;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import com.huertos.model.TipoMaterialVegetal;
import com.huertos.model.UnidadMedida;
import com.huertos.service.BodegaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BodegaController.class)
@Import(SecurityConfig.class)
class BodegaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private BodegaService bodegaService;

    private String tokenResponsable;
    private String tokenMiembro;
    private String tokenTecnicoMsp;

    @BeforeEach
    void setUp() {
        tokenResponsable = jwtService.generarToken("responsable@correo.com", "RESPONSABLE_COMUNIDAD");
        tokenMiembro = jwtService.generarToken("miembro@correo.com", "MIEMBRO");
        tokenTecnicoMsp = jwtService.generarToken("tecnico@msp.gob.ec", "TECNICO_MSP");
    }

    @Test
    void registrarIngresoMaterialConRolResponsableComunidadDevuelve201() throws Exception {
        MaterialVegetalResponse respuesta = new MaterialVegetalResponse(
                1L,
                10L,
                5L,
                "Menta",
                "Mentha spicata",
                TipoMaterialVegetal.ESQUEJES,
                new BigDecimal("15.50"),
                UnidadMedida.GRAMOS,
                "Lote A",
                LocalDate.of(2026, 11, 11)
        );

        when(bodegaService.obtenerComunidadIdPorCorreo("responsable@correo.com")).thenReturn(10L);
        when(bodegaService.registrarIngresoMaterial(eq(10L), any(RegistrarMaterialVegetalRequest.class)))
                .thenReturn(respuesta);

        String json = """
                {
                    "plantaId": 5,
                    "tipoMaterial": "ESQUEJES",
                    "cantidad": 15.50,
                    "unidadMedida": "GRAMOS",
                    "loteOrigen": "Lote A",
                    "fechaIngreso": "2026-11-11"
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.comunidadId").value(10L))
                .andExpect(jsonPath("$.plantaId").value(5L))
                .andExpect(jsonPath("$.nombreComunPlanta").value("Menta"))
                .andExpect(jsonPath("$.cantidadDisponible").value(15.50))
                .andExpect(jsonPath("$.unidadMedida").value("GRAMOS"));
    }

    @Test
    void registrarIngresoMaterialSinAutenticacionDevuelve403() throws Exception {
        String json = """
                {
                    "plantaId": 5,
                    "tipoMaterial": "ESQUEJES",
                    "cantidad": 15.50,
                    "unidadMedida": "GRAMOS",
                    "fechaIngreso": "2026-11-11"
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarIngresoMaterialConRolNoAutorizadoDevuelve403() throws Exception {
        String json = """
                {
                    "plantaId": 5,
                    "tipoMaterial": "ESQUEJES",
                    "cantidad": 15.50,
                    "unidadMedida": "GRAMOS",
                    "fechaIngreso": "2026-11-11"
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMiembro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarIngresoMaterialConCantidadCeroDevuelve400() throws Exception {
        String json = """
                {
                    "plantaId": 5,
                    "tipoMaterial": "ESQUEJES",
                    "cantidad": 0,
                    "unidadMedida": "GRAMOS",
                    "fechaIngreso": "2026-11-11"
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cantidad").value("La cantidad debe ser mayor a cero"));
    }

    @Test
    void listarMaterialPorComunidadConRolResponsableComunidadDevuelve200() throws Exception {
        MaterialVegetalResponse respuesta = new MaterialVegetalResponse(
                1L, 10L, 5L, "Menta", "Mentha spicata",
                TipoMaterialVegetal.ESQUEJES, new BigDecimal("15.50"),
                UnidadMedida.GRAMOS, "Lote A", LocalDate.of(2026, 11, 11)
        );

        when(bodegaService.listarMaterialPorComunidad(eq(10L), eq("responsable@correo.com"), eq(false)))
                .thenReturn(List.of(respuesta));

        mockMvc.perform(get("/api/bodega/comunidades/10/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombreComunPlanta").value("Menta"))
                .andExpect(jsonPath("$[0].cantidadDisponible").value(15.50));
    }

    @Test
    void listarMaterialPorComunidadConRolTecnicoMspDevuelve200() throws Exception {
        when(bodegaService.listarMaterialPorComunidad(eq(10L), eq("tecnico@msp.gob.ec"), eq(true)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/bodega/comunidades/10/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenTecnicoMsp))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listarMaterialPorComunidadSinAutenticacionDevuelve403() throws Exception {
        mockMvc.perform(get("/api/bodega/comunidades/10/material-vegetal"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarMaterialPorComunidadConRolNoAutorizadoDevuelve403() throws Exception {
        mockMvc.perform(get("/api/bodega/comunidades/10/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMiembro))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarMaterialPorComunidadAccesoDenegadoDevuelve403() throws Exception {
        when(bodegaService.listarMaterialPorComunidad(eq(99L), anyString(), anyBoolean()))
                .thenThrow(new org.springframework.security.access.AccessDeniedException(
                        "No tiene permisos para consultar el inventario de otra comunidad"
                ));

        mockMvc.perform(get("/api/bodega/comunidades/99/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(
                        "No tiene permisos para consultar el inventario de otra comunidad"
                ));
    }
}

