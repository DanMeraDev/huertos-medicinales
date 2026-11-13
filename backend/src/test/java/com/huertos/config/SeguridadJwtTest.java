package com.huertos.config;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.controller.PingController;
import com.huertos.model.Rol;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.PingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PingController.class)
@Import(SecurityConfig.class)
class SeguridadJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private PingService pingService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void conTokenValidoPasaLaSeguridad() throws Exception {
        String token = jwtService.generarToken(registrar(usuario(Rol.RESPONSABLE_COMUNIDAD)));

        mockMvc.perform(get("/api/ruta-inexistente").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void conTokenInvalidoDevuelve403() throws Exception {
        mockMvc.perform(get("/api/ruta-inexistente").header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinTokenDevuelve403() throws Exception {
        mockMvc.perform(get("/api/ruta-inexistente"))
                .andExpect(status().isForbidden());
    }

    @Test
    void lasRutasDeAccesoAntiguasYaNoSonPublicas() throws Exception {
        mockMvc.perform(get("/api/auth/login")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/usuarios/registro")).andExpect(status().isForbidden());
    }

    @Test
    void elTokenDeUnaCuentaDesactivadaDevuelve403() throws Exception {
        Usuario usuario = usuario(Rol.RESPONSABLE_COMUNIDAD);
        String token = jwtService.generarToken(usuario);
        usuario.setActivo(false);
        registrar(usuario);

        mockMvc.perform(get("/api/ruta-inexistente").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void elTokenEmitidoAntesDeCambiarLaClaveDevuelve403() throws Exception {
        Usuario usuario = usuario(Rol.RESPONSABLE_COMUNIDAD);
        String token = jwtService.generarToken(usuario);
        usuario.setPasswordHash("$2a$10$hashDeLaClaveNueva");
        registrar(usuario);

        mockMvc.perform(get("/api/ruta-inexistente").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void conCambioDeClavePendienteSoloPermiteCambiarLaClave() throws Exception {
        Usuario usuario = usuario(Rol.RESPONSABLE_COMUNIDAD);
        usuario.setRequiereCambioClave(true);
        String token = jwtService.generarToken(registrar(usuario));

        mockMvc.perform(get("/api/comunidad/huerto").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/usuarios/cambiar-clave").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void unRolEquivocadoRecibe403AunqueElCuerpoSeaInvalido() throws Exception {
        String token = jwtService.generarToken(registrar(usuario(Rol.TECNICO_MSP)));

        mockMvc.perform(post("/api/comunidad/siembras")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    private Usuario usuario(Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(15L);
        usuario.setCorreo("rosa@correo.com");
        usuario.setPasswordHash("$2a$10$hashDeLaClaveActual");
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuario;
    }

    private Usuario registrar(Usuario usuario) {
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        return usuario;
    }
}
