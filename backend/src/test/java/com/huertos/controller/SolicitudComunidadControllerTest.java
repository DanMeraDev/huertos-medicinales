package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.repository.UsuarioRepository;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.RechazarSolicitudRequest;
import com.huertos.dto.ResolucionSolicitudResponse;
import com.huertos.dto.SolicitudResumenResponse;
import com.huertos.model.EstadoSolicitud;
import com.huertos.service.SolicitudComunidadService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(SolicitudComunidadController.class)
@Import(SecurityConfig.class)
class SolicitudComunidadControllerTest {

    private static final String CORREO_TECNICO = "tecnico.msp@huertos.gob.ec";
    private static final String MOTIVO_VALIDO = """
            {"motivoRechazo":"La ubicación indicada no cuenta con acceso seguro a fuente de agua."}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitudComunidadService solicitudComunidadService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void listaLasSolicitudesPendientes() throws Exception {
        when(solicitudComunidadService.listarPorEstado(EstadoSolicitud.PENDIENTE)).thenReturn(List.of(
                new SolicitudResumenResponse(12L, "Segundo Tránsito Amaguaña", LocalDate.of(2026, 11, 10), "Pichincha")));

        mockMvc.perform(get("/api/solicitudes-comunidad").param("estado", "PENDIENTE").with(tecnicoMsp()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].solicitante").value("Segundo Tránsito Amaguaña"))
                .andExpect(jsonPath("$[0].fecha").value("2026-11-10"));
    }

    @Test
    void apruebaLaSolicitudConElTecnicoAutenticado() throws Exception {
        when(solicitudComunidadService.aprobarSolicitud(12L, CORREO_TECNICO)).thenReturn(
                new ResolucionSolicitudResponse(12L, EstadoSolicitud.APROBADA, "Solicitud aprobada", 3L, 8L));

        mockMvc.perform(post("/api/solicitudes-comunidad/12/aprobar").with(tecnicoMsp()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.comunidadId").value(3))
                .andExpect(jsonPath("$.usuarioId").value(8));
    }

    @Test
    void rechazaLaSolicitudConUnMotivoValido() throws Exception {
        when(solicitudComunidadService.rechazarSolicitud(eq(12L), any(RechazarSolicitudRequest.class),
                eq(CORREO_TECNICO))).thenReturn(
                new ResolucionSolicitudResponse(12L, EstadoSolicitud.RECHAZADA, "Solicitud rechazada", null, null));

        mockMvc.perform(post("/api/solicitudes-comunidad/12/rechazar").with(tecnicoMsp())
                        .contentType(MediaType.APPLICATION_JSON).content(MOTIVO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
    }

    @Test
    void exigeUnMotivoDeAlMenos20Caracteres() throws Exception {
        mockMvc.perform(post("/api/solicitudes-comunidad/12/rechazar").with(tecnicoMsp())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivoRechazo\":\"Muy corto\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.motivoRechazo").exists());
    }

    @Test
    void niegaElAccesoAOtrosRoles() throws Exception {
        mockMvc.perform(get("/api/solicitudes-comunidad").param("estado", "PENDIENTE")
                        .with(user("rosa@huertos.gob.ec").roles("RESPONSABLE_COMUNIDAD")))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor tecnicoMsp() {
        return user(CORREO_TECNICO).roles("TECNICO_MSP");
    }
}
