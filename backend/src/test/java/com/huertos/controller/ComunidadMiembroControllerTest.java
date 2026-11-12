package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.CambiarEstadoMiembroRequest;
import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.MiembroEstadoResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.model.Genero;
import com.huertos.model.NivelMiembro;
import com.huertos.service.MiembroService;
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

@WebMvcTest(ComunidadMiembroController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ComunidadMiembroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private MiembroService miembroService;

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarMiembroRetorna201ConDatosValidos() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        MiembroDetalleResponse response = new MiembroDetalleResponse(
                15L,
                3L,
                "1710034065",
                "María Tránsito Amaguaña Alba",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                16,
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                0,
                true,
                LocalDate.of(2026, 11, 12)
        );

        when(miembroService.registrarMiembroPorCorreo(eq("responsable@huertos.gob.ec"), any(RegistrarMiembroRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.comunidadId").value(3))
                .andExpect(jsonPath("$.cedula").value("1710034065"))
                .andExpect(jsonPath("$.nombreCompleto").value("María Tránsito Amaguaña Alba"))
                .andExpect(jsonPath("$.edad").value(16))
                .andExpect(jsonPath("$.genero").value("FEMENINO"))
                .andExpect(jsonPath("$.telefono").value("0981234567"))
                .andExpect(jsonPath("$.nivel").value("APRENDIZ"))
                .andExpect(jsonPath("$.puntosTotales").value(0))
                .andExpect(jsonPath("$.activoEnComunidad").value(true));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarMiembroRetorna400CuandoConsentimientoEsFalso() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                false
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.consentimientoDatos").value("Debe certificar la autorización del voluntario o tutor legal"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarMiembroRetorna400CuandoCedulaEsInvalida() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "17100340",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cedula").value("La cédula debe tener 10 dígitos numéricos"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarMiembroRetorna400CuandoTelefonoEsInvalido() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0881234567",
                NivelMiembro.APRENDIZ,
                true
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.telefono").value("El teléfono debe iniciar con 09 y tener 10 dígitos"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarMiembroRetorna400CuandoNombresTienenNumeros() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María 123",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombres").value("Los nombres solo admiten letras y espacios"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarMiembroRetorna409CuandoYaEstaActivoEnComunidad() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        when(miembroService.registrarMiembroPorCorreo(eq("responsable@huertos.gob.ec"), any(RegistrarMiembroRequest.class)))
                .thenThrow(new ConflictoException("El voluntario con la cédula ingresada ya se encuentra registrado y activo en esta comunidad"));

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El voluntario con la cédula ingresada ya se encuentra registrado y activo en esta comunidad"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void registrarMiembroRetorna404CuandoNoTieneComunidadAsignada() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        when(miembroService.registrarMiembroPorCorreo(eq("responsable@huertos.gob.ec"), any(RegistrarMiembroRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("No se encontró ninguna comunidad asociada al responsable autenticado"));

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No se encontró ninguna comunidad asociada al responsable autenticado"));
    }

    @Test
    @WithMockUser(username = "admin@huertos.gob.ec", roles = "ADMINISTRADOR")
    void registrarMiembroRetorna403CuandoUsuarioNoTieneRolResponsable() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarMiembroRetorna403CuandoNoEstaAutenticado() throws Exception {
        RegistrarMiembroRequest request = new RegistrarMiembroRequest(
                "1710034065",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                true
        );

        mockMvc.perform(post("/api/comunidades/mi-comunidad/miembros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void cambiarEstadoRetorna200AlDarDeBajaExitosamente() throws Exception {
        CambiarEstadoMiembroRequest request = new CambiarEstadoMiembroRequest(false, "Cambio de domicilio");
        MiembroEstadoResponse response = new MiembroEstadoResponse(
                15L,
                3L,
                "María Tránsito Amaguaña Alba",
                false,
                LocalDate.of(2026, 11, 12),
                "Cambio de domicilio",
                "El miembro ha sido dado de baja de la comunidad exitosamente"
        );

        when(miembroService.cambiarEstadoPorCorreo("responsable@huertos.gob.ec", 15L, request))
                .thenReturn(response);

        mockMvc.perform(patch("/api/comunidades/mi-comunidad/miembros/15/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miembroId").value(15))
                .andExpect(jsonPath("$.comunidadId").value(3))
                .andExpect(jsonPath("$.nombreMiembro").value("María Tránsito Amaguaña Alba"))
                .andExpect(jsonPath("$.activoEnComunidad").value(false))
                .andExpect(jsonPath("$.fechaBaja").value("2026-11-12"))
                .andExpect(jsonPath("$.motivoBaja").value("Cambio de domicilio"))
                .andExpect(jsonPath("$.mensaje").value("El miembro ha sido dado de baja de la comunidad exitosamente"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void cambiarEstadoRetorna200AlReactivarExitosamente() throws Exception {
        CambiarEstadoMiembroRequest request = new CambiarEstadoMiembroRequest(true, null);
        MiembroEstadoResponse response = new MiembroEstadoResponse(
                15L,
                3L,
                "María Tránsito Amaguaña Alba",
                true,
                null,
                null,
                "El miembro ha sido reactivado en la comunidad exitosamente"
        );

        when(miembroService.cambiarEstadoPorCorreo("responsable@huertos.gob.ec", 15L, request))
                .thenReturn(response);

        mockMvc.perform(patch("/api/comunidades/mi-comunidad/miembros/15/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miembroId").value(15))
                .andExpect(jsonPath("$.activoEnComunidad").value(true))
                .andExpect(jsonPath("$.fechaBaja").doesNotExist())
                .andExpect(jsonPath("$.motivoBaja").doesNotExist())
                .andExpect(jsonPath("$.mensaje").value("El miembro ha sido reactivado en la comunidad exitosamente"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void cambiarEstadoRetorna400CuandoActivoEsNulo() throws Exception {
        String payload = "{\"activo\":null,\"motivoBaja\":null}";

        mockMvc.perform(patch("/api/comunidades/mi-comunidad/miembros/15/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.activo").value("El estado activo es obligatorio"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void cambiarEstadoRetorna400CuandoMotivoBajaExcede255Caracteres() throws Exception {
        CambiarEstadoMiembroRequest request = new CambiarEstadoMiembroRequest(false, "A".repeat(256));

        mockMvc.perform(patch("/api/comunidades/mi-comunidad/miembros/15/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.motivoBaja").value("El motivo de baja no puede superar 255 caracteres"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void cambiarEstadoRetorna404CuandoMiembroNoPerteneceAComunidad() throws Exception {
        CambiarEstadoMiembroRequest request = new CambiarEstadoMiembroRequest(false, "Motivo válido");

        when(miembroService.cambiarEstadoPorCorreo("responsable@huertos.gob.ec", 99L, request))
                .thenThrow(new RecursoNoEncontradoException("El miembro especificado no pertenece a tu comunidad"));

        mockMvc.perform(patch("/api/comunidades/mi-comunidad/miembros/99/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("El miembro especificado no pertenece a tu comunidad"));
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void cambiarEstadoRetorna409CuandoMiembroYaEstaEnEstadoSolicitado() throws Exception {
        CambiarEstadoMiembroRequest request = new CambiarEstadoMiembroRequest(false, "Motivo válido");

        when(miembroService.cambiarEstadoPorCorreo("responsable@huertos.gob.ec", 15L, request))
                .thenThrow(new ConflictoException("El voluntario ya se encuentra en el estado solicitado"));

        mockMvc.perform(patch("/api/comunidades/mi-comunidad/miembros/15/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El voluntario ya se encuentra en el estado solicitado"));
    }

    @Test
    @WithMockUser(username = "admin@huertos.gob.ec", roles = "ADMINISTRADOR")
    void cambiarEstadoRetorna403CuandoUsuarioNoTieneRolResponsable() throws Exception {
        CambiarEstadoMiembroRequest request = new CambiarEstadoMiembroRequest(false, "Motivo");

        mockMvc.perform(patch("/api/comunidades/mi-comunidad/miembros/15/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cambiarEstadoRetorna403CuandoNoEstaAutenticado() throws Exception {
        CambiarEstadoMiembroRequest request = new CambiarEstadoMiembroRequest(false, "Motivo");

        mockMvc.perform(patch("/api/comunidades/mi-comunidad/miembros/15/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "responsable@huertos.gob.ec", roles = "RESPONSABLE_COMUNIDAD")
    void listarMiembrosRetorna200ConLista() throws Exception {
        MiembroDetalleResponse miembro1 = new MiembroDetalleResponse(
                15L,
                3L,
                "1710034065",
                "María Tránsito Amaguaña Alba",
                "María Tránsito",
                "Amaguaña Alba",
                LocalDate.of(2010, 3, 25),
                16,
                Genero.FEMENINO,
                "0981234567",
                NivelMiembro.APRENDIZ,
                50,
                true,
                LocalDate.of(2026, 11, 10)
        );

        when(miembroService.listarMiembrosPorCorreo("responsable@huertos.gob.ec"))
                .thenReturn(List.of(miembro1));

        mockMvc.perform(get("/api/comunidades/mi-comunidad/miembros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(15))
                .andExpect(jsonPath("$[0].nombreCompleto").value("María Tránsito Amaguaña Alba"))
                .andExpect(jsonPath("$[0].activoEnComunidad").value(true));
    }

    @Test
    @WithMockUser(username = "admin@huertos.gob.ec", roles = "ADMINISTRADOR")
    void listarMiembrosRetorna403CuandoUsuarioNoTieneRolResponsable() throws Exception {
        mockMvc.perform(get("/api/comunidades/mi-comunidad/miembros"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarMiembrosRetorna403CuandoNoEstaAutenticado() throws Exception {
        mockMvc.perform(get("/api/comunidades/mi-comunidad/miembros"))
                .andExpect(status().isForbidden());
    }
}
