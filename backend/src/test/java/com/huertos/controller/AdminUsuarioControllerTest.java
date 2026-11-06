package com.huertos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.JwtService;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.ActualizarRolRequest;
import com.huertos.dto.UsuarioResponse;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.exception.ReglaNegocioException;
import com.huertos.model.Rol;
import com.huertos.service.AdminUsuarioService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@WebMvcTest(AdminUsuarioController.class)
@Import(SecurityConfig.class)
class AdminUsuarioControllerTest {

    private static final String EMAIL_ADMIN = "admin@correo.com";
    private static final UsuarioResponse USUARIO = new UsuarioResponse(
            2L, "Rosa Quishpe", "1712345678", "rosa@correo.com", Rol.RESPONSABLE_COMUNIDAD, true,
            LocalDateTime.of(2026, 11, 5, 10, 0));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AdminUsuarioService adminUsuarioService;

    @Test
    void listaLosUsuariosConRolAdministrador() throws Exception {
        when(adminUsuarioService.listarUsuarios()).thenReturn(List.of(USUARIO));

        mockMvc.perform(conToken(get("/api/admin/usuarios"), "ADMIN_MSP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("rosa@correo.com"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void actualizaElRolConLaIdentidadDelToken() throws Exception {
        when(adminUsuarioService.actualizarRol(
                2L, new ActualizarRolRequest(Rol.RESPONSABLE_COMUNIDAD), EMAIL_ADMIN)).thenReturn(USUARIO);

        mockMvc.perform(cambioDeRol(2L, "{\"nuevoRol\":\"RESPONSABLE_COMUNIDAD\"}", "ADMIN_MSP"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("RESPONSABLE_COMUNIDAD"));
    }

    @Test
    void unMiembroRecibe403ConProblemDetail() throws Exception {
        mockMvc.perform(conToken(get("/api/admin/usuarios"), "MIEMBRO"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("No tienes permisos para acceder a este módulo."));
    }

    @Test
    void sinSesionDevuelve403() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unRolNuloDevuelve400ConElCampo() throws Exception {
        mockMvc.perform(cambioDeRol(2L, "{}", "ADMIN_MSP"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nuevoRol").exists());
    }

    @Test
    void unRolFueraDelEnumDevuelve400() throws Exception {
        mockMvc.perform(cambioDeRol(2L, "{\"nuevoRol\":\"JARDINERO\"}", "ADMIN_MSP"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unUsuarioInexistenteDevuelve404() throws Exception {
        when(adminUsuarioService.actualizarRol(eq(9L), any(), eq(EMAIL_ADMIN)))
                .thenThrow(new RecursoNoEncontradoException("No existe un usuario con el ID especificado."));

        mockMvc.perform(cambioDeRol(9L, "{\"nuevoRol\":\"MIEMBRO\"}", "ADMIN_MSP"))
                .andExpect(status().isNotFound());
    }

    @Test
    void degradarAlUnicoAdministradorDevuelve400() throws Exception {
        when(adminUsuarioService.actualizarRol(eq(1L), any(), eq(EMAIL_ADMIN)))
                .thenThrow(new ReglaNegocioException(
                        "No es posible cambiar el rol del único administrador activo del sistema."));

        mockMvc.perform(cambioDeRol(1L, "{\"nuevoRol\":\"MIEMBRO\"}", "ADMIN_MSP"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("No es posible cambiar el rol del único administrador activo del sistema."));
    }

    private MockHttpServletRequestBuilder cambioDeRol(Long id, String cuerpo, String rol) {
        return conToken(patch("/api/admin/usuarios/{id}/rol", id), rol)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo);
    }

    private MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder peticion, String rol) {
        return peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generarToken(EMAIL_ADMIN, rol));
    }
}
