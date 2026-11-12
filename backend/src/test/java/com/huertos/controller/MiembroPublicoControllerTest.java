package com.huertos.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.InsigniaObtenidaDto;
import com.huertos.dto.PerfilPublicoMiembroResponse;
import com.huertos.exception.GlobalExceptionHandler;
import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.model.NivelMiembro;
import com.huertos.service.MiembroService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MiembroPublicoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MiembroPublicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MiembroService miembroService;

    @Test
    void obtenerPerfilPorCedulaRetorna200SinAutenticacionYSinDatosPrivados() throws Exception {
        PerfilPublicoMiembroResponse response = new PerfilPublicoMiembroResponse(
                "María Tránsito Amaguaña Alba",
                NivelMiembro.OFICIAL,
                120,
                List.of("Huerto Comunitario Allpa Viva", "Sembradores de San Antonio"),
                List.of(
                        new InsigniaObtenidaDto(
                                "Maestro del Abonaje 1",
                                "Completó 50 sesiones de preparación y distribución de compost",
                                "TALENTO_COMUNIDAD",
                                LocalDate.of(2026, 11, 5)
                        ),
                        new InsigniaObtenidaDto(
                                "Guardián de la Chakra Kiwa",
                                "Reconocimiento oficial MSP en cultivo y cosecha de plantas medicinales",
                                "OFICIAL_SABER_MSP",
                                LocalDate.of(2026, 10, 20)
                        )
                )
        );

        when(miembroService.obtenerPerfilPublicoPorCedula("1710034065")).thenReturn(response);

        mockMvc.perform(get("/api/publico/miembros/cedula/1710034065"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreCompleto").value("María Tránsito Amaguaña Alba"))
                .andExpect(jsonPath("$.nivel").value("OFICIAL"))
                .andExpect(jsonPath("$.puntosTotales").value(120))
                .andExpect(jsonPath("$.comunidades.length()").value(2))
                .andExpect(jsonPath("$.comunidades[0]").value("Huerto Comunitario Allpa Viva"))
                .andExpect(jsonPath("$.insignias.length()").value(2))
                .andExpect(jsonPath("$.insignias[0].nombre").value("Maestro del Abonaje 1"))
                .andExpect(jsonPath("$.insignias[0].tipo").value("TALENTO_COMUNIDAD"))
                .andExpect(jsonPath("$.insignias[0].fechaObtencion").value("2026-11-05"))
                .andExpect(jsonPath("$.telefono").doesNotExist())
                .andExpect(jsonPath("$.correo").doesNotExist())
                .andExpect(jsonPath("$.fechaNacimiento").doesNotExist())
                .andExpect(jsonPath("$.cedula").doesNotExist());

        verify(miembroService).obtenerPerfilPublicoPorCedula("1710034065");
    }

    @Test
    void obtenerPerfilPorCedulaRetorna400CuandoFormatoNoEsDe10Digitos() throws Exception {
        mockMvc.perform(get("/api/publico/miembros/cedula/17100A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La cédula debe contener exactamente 10 dígitos numéricos"));

        verify(miembroService, never()).obtenerPerfilPublicoPorCedula(anyString());
    }

    @Test
    void obtenerPerfilPorCedulaRetorna400CuandoLongitudEsMenorA10Digitos() throws Exception {
        mockMvc.perform(get("/api/publico/miembros/cedula/1710034"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La cédula debe contener exactamente 10 dígitos numéricos"));

        verify(miembroService, never()).obtenerPerfilPublicoPorCedula(anyString());
    }

    @Test
    void obtenerPerfilPorCedulaRetorna404CuandoVoluntarioNoExiste() throws Exception {
        String mensaje = "No se encontró ningún voluntario registrado con la cédula ingresada. Verifica el número o anímate a registrar una nueva comunidad";
        when(miembroService.obtenerPerfilPublicoPorCedula("9999999999"))
                .thenThrow(new RecursoNoEncontradoException(mensaje));

        mockMvc.perform(get("/api/publico/miembros/cedula/9999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(mensaje));
    }
}
