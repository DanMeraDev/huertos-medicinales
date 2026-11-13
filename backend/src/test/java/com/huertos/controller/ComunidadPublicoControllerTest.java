package com.huertos.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.repository.UsuarioRepository;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.DirectorioComunidadResponse;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.service.ComunidadService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ComunidadPublicoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ComunidadPublicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ComunidadService comunidadService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void obtenerDirectorioRetorna200SinAutenticacionConListado() throws Exception {
        DirectorioComunidadResponse item = new DirectorioComunidadResponse(
                3L,
                "Huerto Comunitario Allpa Viva",
                "Segundo Tránsito Amaguaña",
                "0991234567",
                "comunidad.allpa@huertos.gob.ec",
                14,
                "Pichincha",
                "Calderon",
                "Calle de las Rosas y Pasaje Los Pinos, lote 14",
                new BigDecimal("-0.0987123"),
                new BigDecimal("-78.4321456"),
                2650
        );

        when(comunidadService.obtenerDirectorioPublico()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/publico/comunidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Huerto Comunitario Allpa Viva"))
                .andExpect(jsonPath("$[0].nombreResponsable").value("Segundo Tránsito Amaguaña"))
                .andExpect(jsonPath("$[0].telefonoContacto").value("0991234567"))
                .andExpect(jsonPath("$[0].correoContacto").value("comunidad.allpa@huertos.gob.ec"))
                .andExpect(jsonPath("$[0].totalMiembros").value(14))
                .andExpect(jsonPath("$[0].provincia").value("Pichincha"))
                .andExpect(jsonPath("$[0].parroquia").value("Calderon"))
                .andExpect(jsonPath("$[0].direccion").value("Calle de las Rosas y Pasaje Los Pinos, lote 14"))
                .andExpect(jsonPath("$[0].latitud").value(-0.0987123))
                .andExpect(jsonPath("$[0].longitud").value(-78.4321456))
                .andExpect(jsonPath("$[0].altitudMsnm").value(2650));

        verify(comunidadService).obtenerDirectorioPublico();
    }

    @Test
    void obtenerDirectorioRetornaListaVaciaCuandoNoExistenComunidades() throws Exception {
        when(comunidadService.obtenerDirectorioPublico()).thenReturn(List.of());

        mockMvc.perform(get("/api/publico/comunidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(comunidadService).obtenerDirectorioPublico();
    }
}
