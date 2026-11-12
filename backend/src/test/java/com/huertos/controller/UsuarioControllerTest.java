package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.CambiarClaveRequest;
import com.huertos.dto.CambioClaveResponse;
import com.huertos.dto.CrearUsuarioRequest;
import com.huertos.dto.UsuarioDetalleResponse;
import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.service.UsuarioService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    private static final String USUARIOS = "/api/usuarios";
    private static final String CUERPO_VALIDO = """
            {"nombreCompleto":"Rosa Elena Chimbolema","identificacion":"1710034065",
             "fechaNacimiento":"1990-05-14","genero":"FEMENINO","correo":"rosa.chimbolema@huertos.gob.ec",
             "telefono":"0987654321","rol":"TECNICO_MSP","password":"PasswordSegura2026"}
            """;

    private static final String CAMBIAR_CLAVE = "/api/usuarios/cambiar-clave";
    private static final String CAMBIO_VALIDO = """
            {"passwordActual":"PasswordSegura2026","nuevaPassword":"NuevaClaveSegura2027",
             "confirmarPassword":"NuevaClaveSegura2027"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void elTecnicoSisCreaUnUsuarioYRecibe201() throws Exception {
        when(usuarioService.crearUsuario(any(CrearUsuarioRequest.class))).thenReturn(new UsuarioDetalleResponse(
                5L, "Rosa Elena Chimbolema", "1710034065", LocalDate.of(1990, 5, 14), Genero.FEMENINO,
                "rosa.chimbolema@huertos.gob.ec", "0987654321", Rol.TECNICO_MSP, true, true,
                LocalDateTime.of(2026, 11, 10, 10, 30)));

        mockMvc.perform(crear("TECNICO_SIS", CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.rol").value("TECNICO_MSP"))
                .andExpect(jsonPath("$.requiereCambioClave").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rechazaDatosIncompletosConUn400() throws Exception {
        mockMvc.perform(crear("TECNICO_SIS", "{\"nombreCompleto\":\"Rosa\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.identificacion").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.rol").exists());
    }

    @Test
    void rechazaUnGeneroInvalidoConUn400() throws Exception {
        mockMvc.perform(crear("TECNICO_SIS", CUERPO_VALIDO.replace("FEMENINO", "DESCONOCIDO")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaUnaContrasenaDebilConUn400() throws Exception {
        mockMvc.perform(crear("TECNICO_SIS", CUERPO_VALIDO.replace("PasswordSegura2026", "passwordsegura")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.password").value(
                        "La contraseña debe tener al menos 12 caracteres, incluyendo al menos una mayúscula, "
                                + "una minúscula y un número"));
    }

    @Test
    void rechazaUnTelefonoQueNoEmpiezaEn09ConUn400() throws Exception {
        mockMvc.perform(crear("TECNICO_SIS", CUERPO_VALIDO.replace("0987654321", "0287654321")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.telefono").exists());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TECNICO_MSP", "RESPONSABLE_COMUNIDAD"})
    void rechazaAQuienNoEsTecnicoSisConUn403(String rol) throws Exception {
        mockMvc.perform(crear(rol, CUERPO_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void elUsuarioAutenticadoCambiaSuClaveYRecibe200() throws Exception {
        when(usuarioService.cambiarClave(eq("rosa@huertos.gob.ec"), any(CambiarClaveRequest.class)))
                .thenReturn(new CambioClaveResponse("Contraseña actualizada exitosamente", false));

        mockMvc.perform(put(CAMBIAR_CLAVE).with(user("rosa@huertos.gob.ec").roles("RESPONSABLE_COMUNIDAD"))
                        .contentType(MediaType.APPLICATION_JSON).content(CAMBIO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Contraseña actualizada exitosamente"))
                .andExpect(jsonPath("$.requiereCambioClave").value(false));
    }

    @Test
    void rechazaElCambioDeClaveSinSesion() throws Exception {
        mockMvc.perform(put(CAMBIAR_CLAVE).contentType(MediaType.APPLICATION_JSON).content(CAMBIO_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    void rechazaUnaNuevaClaveDebilConUn400() throws Exception {
        mockMvc.perform(put(CAMBIAR_CLAVE).with(user("rosa@huertos.gob.ec").roles("TECNICO_MSP"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CAMBIO_VALIDO.replace("NuevaClaveSegura2027", "corta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nuevaPassword").exists());
    }

    private MockHttpServletRequestBuilder crear(String rol, String cuerpo) {
        return post(USUARIOS).with(user("persona@huertos.gob.ec").roles(rol))
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }
}
