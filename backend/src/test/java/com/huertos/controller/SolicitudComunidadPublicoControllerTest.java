package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.repository.UsuarioRepository;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.SolicitudComunidadResponse;
import com.huertos.model.EstadoSolicitud;
import com.huertos.model.TipoOrganizacion;
import com.huertos.service.SolicitudComunidadService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(SolicitudComunidadPublicoController.class)
@Import(SecurityConfig.class)
class SolicitudComunidadPublicoControllerTest {

    private static final String CUERPO_VALIDO = """
            {"nombreComunidad":"Huerto Comunitario Allpa Viva","tipoOrganizacion":"BARRIO",
             "provincia":"Pichincha","parroquia":"Calderon",
             "direccion":"Calle de las Rosas y Pasaje Los Pinos, lote 14",
             "latitud":-0.0987123,"longitud":-78.4321456,"altitudMsnm":2650,"disenoPrincipal":"EN_TIERRA",
             "subtiposSolicitados":"SURCOS_WACHU,TERRAZAS_ANTI","cedulaSolicitante":"1710034065",
             "nombreSolicitante":"Segundo Tránsito Amaguaña","fechaNacimiento":"1990-05-14",
             "generoSolicitante":"MASCULINO","correoSolicitante":"comunidad.allpa@huertos.gob.ec",
             "celularSolicitante":"0991234567","consentimientoDatos":true}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitudComunidadService solicitudComunidadService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void registraLaSolicitudSinTokenYDevuelve201() throws Exception {
        when(solicitudComunidadService.registrarSolicitud(any(CrearSolicitudComunidadRequest.class)))
                .thenReturn(new SolicitudComunidadResponse(12L, "Huerto Comunitario Allpa Viva",
                        TipoOrganizacion.BARRIO, "Pichincha", "Calderon", "Segundo Tránsito Amaguaña",
                        "comunidad.allpa@huertos.gob.ec", EstadoSolicitud.PENDIENTE,
                        LocalDateTime.of(2026, 11, 10, 11, 0)));

        enviar(CUERPO_VALIDO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void rechazaCamposVaciosConUn400() throws Exception {
        enviar("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreComunidad").exists())
                .andExpect(jsonPath("$.errores.fechaNacimiento").exists())
                .andExpect(jsonPath("$.errores.consentimientoDatos").exists());
    }

    @Test
    void rechazaLaFaltaDeConsentimientoConUn400() throws Exception {
        enviar(CUERPO_VALIDO.replace("\"consentimientoDatos\":true", "\"consentimientoDatos\":false"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.consentimientoDatos").value(
                        "Debe autorizar el tratamiento de datos personales bajo los términos de la LOPDP"));
    }

    @Test
    void rechazaUnCelularQueNoEmpiezaEn09ConUn400() throws Exception {
        enviar(CUERPO_VALIDO.replace("0991234567", "0291234567"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.celularSolicitante").exists());
    }

    @Test
    void rechazaUnNombreDeComunidadConNumerosConUn400() throws Exception {
        enviar(CUERPO_VALIDO.replace("Huerto Comunitario Allpa Viva", "Huerto 123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombreComunidad").exists());
    }

    @Test
    void rechazaCoordenadasFueraDelEcuadorConUn400() throws Exception {
        enviar(CUERPO_VALIDO.replace("-78.4321456", "-60.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.longitud").value(
                        "Las coordenadas geográficas ingresadas no corresponden al territorio ecuatoriano"));
    }

    private ResultActions enviar(String cuerpo) throws Exception {
        return mockMvc.perform(post("/api/publico/solicitudes-comunidad")
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }
}
