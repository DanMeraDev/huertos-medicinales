package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.JwtService;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.BajaMaterialResponse;
import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarBajaMaterialRequest;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import com.huertos.model.CausaFracaso;
import com.huertos.model.TipoMaterialVegetal;
import com.huertos.model.UnidadMedida;
import com.huertos.service.BodegaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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
    private String tokenTecnico;

    @BeforeEach
    void setUp() {
        tokenResponsable = jwtService.generarToken("responsable@correo.com", "RESPONSABLE_COMUNIDAD");
        tokenMiembro = jwtService.generarToken("miembro@correo.com", "MIEMBRO");
        tokenTecnico = jwtService.generarToken("tecnico@msp.gob.ec", "TECNICO_MSP");
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
        MaterialVegetalResponse item = new MaterialVegetalResponse(
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

        doNothing().when(bodegaService).validarAccesoComunidad(eq(10L), any(Authentication.class));
        when(bodegaService.listarMaterialPorComunidad(10L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/bodega/comunidades/10/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].comunidadId").value(10L))
                .andExpect(jsonPath("$[0].plantaId").value(5L))
                .andExpect(jsonPath("$[0].nombreComunPlanta").value("Menta"))
                .andExpect(jsonPath("$[0].nombreCientificoPlanta").value("Mentha spicata"))
                .andExpect(jsonPath("$[0].tipoMaterial").value("ESQUEJES"))
                .andExpect(jsonPath("$[0].cantidadDisponible").value(15.50))
                .andExpect(jsonPath("$[0].unidadMedida").value("GRAMOS"))
                .andExpect(jsonPath("$[0].loteOrigen").value("Lote A"))
                .andExpect(jsonPath("$[0].fechaIngreso").value("2026-11-11"));
    }

    @Test
    void listarMaterialPorComunidadConRolTecnicoMspDevuelve200() throws Exception {
        doNothing().when(bodegaService).validarAccesoComunidad(eq(10L), any(Authentication.class));
        when(bodegaService.listarMaterialPorComunidad(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/bodega/comunidades/10/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenTecnico))
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
    void listarMaterialPorComunidadDeOtraComunidadDevuelve403() throws Exception {
        doThrow(new AccessDeniedException("No tiene permisos para consultar el inventario de otra comunidad"))
                .when(bodegaService).validarAccesoComunidad(eq(99L), any(Authentication.class));

        mockMvc.perform(get("/api/bodega/comunidades/99/material-vegetal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("No tiene permisos para consultar el inventario de otra comunidad"));
    }

    @Test
    void registrarBajaMaterialConRolResponsableComunidadDevuelve201() throws Exception {
        BajaMaterialResponse respuesta = new BajaMaterialResponse(
                1L,
                10L,
                new BigDecimal("5.00"),
                CausaFracaso.PLAGA_ENFERMEDAD,
                "Afectado por plaga",
                LocalDateTime.of(2026, 11, 12, 10, 0)
        );

        when(bodegaService.registrarBajaMaterial(eq(10L), any(RegistrarBajaMaterialRequest.class)))
                .thenReturn(respuesta);

        String json = """
                {
                    "cantidadBaja": 5.00,
                    "causaFracaso": "PLAGA_ENFERMEDAD",
                    "observacion": "Afectado por plaga"
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal/10/bajas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bodegaMaterialId").value(10L))
                .andExpect(jsonPath("$.cantidadBaja").value(5.00))
                .andExpect(jsonPath("$.causaFracaso").value("PLAGA_ENFERMEDAD"))
                .andExpect(jsonPath("$.observacion").value("Afectado por plaga"))
                .andExpect(jsonPath("$.fechaBaja").value("2026-11-12T10:00:00"));
    }

    @Test
    void registrarBajaMaterialSinAutenticacionDevuelve403() throws Exception {
        String json = """
                {
                    "cantidadBaja": 5.00,
                    "causaFracaso": "PLAGA_ENFERMEDAD",
                    "observacion": "Afectado por plaga"
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal/10/bajas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarBajaMaterialConRolNoAutorizadoDevuelve403() throws Exception {
        String json = """
                {
                    "cantidadBaja": 5.00,
                    "causaFracaso": "PLAGA_ENFERMEDAD"
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal/10/bajas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenMiembro)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarBajaMaterialConCantidadInvalidaDevuelve400() throws Exception {
        String json = """
                {
                    "cantidadBaja": -1,
                    "causaFracaso": "PLAGA_ENFERMEDAD"
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal/10/bajas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cantidadBaja").value("La cantidad a dar de baja debe ser mayor a cero"));
    }

    @Test
    void registrarBajaMaterialSinCausaFracasoDevuelve400() throws Exception {
        String json = """
                {
                    "cantidadBaja": 5.00
                }
                """;

        mockMvc.perform(post("/api/bodega/material-vegetal/10/bajas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponsable)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.causaFracaso").value("Debe seleccionar una causa de fracaso válida para el registro de la baja"));
    }
}
