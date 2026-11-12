package com.huertos.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.LoginRequest;
import com.huertos.dto.LoginResponse;
import com.huertos.exception.CredencialesInvalidasException;
import com.huertos.exception.CuentaInactivaException;
import com.huertos.model.Rol;
import com.huertos.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    private static final String LOGIN = "/api/publico/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void iniciaSesionSinTokenPrevioYDevuelve200() throws Exception {
        when(authService.iniciarSesion(new LoginRequest("rosa@correo.com", "Password123!")))
                .thenReturn(new LoginResponse("token-jwt", "Bearer", 1L, "rosa@correo.com", "Rosa Quishpe",
                        Rol.RESPONSABLE_COMUNIDAD, false));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"rosa@correo.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("RESPONSABLE_COMUNIDAD"))
                .andExpect(jsonPath("$.requiereCambioClave").value(false));
    }

    @Test
    void rechazaCamposVaciosConUn400() throws Exception {
        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").value("El correo es obligatorio"))
                .andExpect(jsonPath("$.errores.password").value("La contraseña es obligatoria"));
    }

    @Test
    void rechazaUnCorreoConFormatoInvalidoConUn400() throws Exception {
        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"rosa-correo\",\"password\":\"Password123!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").value("Formato de correo inválido"));
    }

    @Test
    void devuelve401ConCredencialesIncorrectas() throws Exception {
        when(authService.iniciarSesion(new LoginRequest("rosa@correo.com", "Mala123!")))
                .thenThrow(new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"rosa@correo.com\",\"password\":\"Mala123!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos"));
    }

    @Test
    void devuelve403ConUnaCuentaInactiva() throws Exception {
        when(authService.iniciarSesion(new LoginRequest("rosa@correo.com", "Password123!")))
                .thenThrow(new CuentaInactivaException("Esta cuenta se encuentra inactiva"));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"rosa@correo.com\",\"password\":\"Password123!\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Esta cuenta se encuentra inactiva"));
    }
}
