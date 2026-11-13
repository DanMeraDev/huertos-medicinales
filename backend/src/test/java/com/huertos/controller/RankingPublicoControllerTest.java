package com.huertos.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.repository.UsuarioRepository;
import com.huertos.config.SecurityConfig;
import com.huertos.dto.RankingComunidadResponse;
import com.huertos.dto.RankingMiembroResponse;
import com.huertos.model.NivelMiembro;
import com.huertos.model.TipoOrganizacion;
import com.huertos.service.RankingService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RankingPublicoController.class)
@Import(SecurityConfig.class)
class RankingPublicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RankingService rankingService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void obtieneRankingDeComunidadesSinToken() throws Exception {
        RankingComunidadResponse com1 = new RankingComunidadResponse(
                1, 10L, "Huerto Allpa", TipoOrganizacion.BARRIO, "Pichincha", "Calderón", 250);
        RankingComunidadResponse com2 = new RankingComunidadResponse(
                2, 20L, "Huerto Kawsay", TipoOrganizacion.FAMILIA, "Imbabura", "Otavalo", 180);

        when(rankingService.obtenerRankingComunidades()).thenReturn(List.of(com1, com2));

        mockMvc.perform(get("/api/publico/ranking/comunidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].posicion").value(1))
                .andExpect(jsonPath("$[0].comunidadId").value(10))
                .andExpect(jsonPath("$[0].nombreComunidad").value("Huerto Allpa"))
                .andExpect(jsonPath("$[0].tipoOrganizacion").value("BARRIO"))
                .andExpect(jsonPath("$[0].provincia").value("Pichincha"))
                .andExpect(jsonPath("$[0].parroquia").value("Calderón"))
                .andExpect(jsonPath("$[0].puntosComunidad").value(250))
                .andExpect(jsonPath("$[1].posicion").value(2))
                .andExpect(jsonPath("$[1].comunidadId").value(20))
                .andExpect(jsonPath("$[1].nombreComunidad").value("Huerto Kawsay"))
                .andExpect(jsonPath("$[1].tipoOrganizacion").value("FAMILIA"))
                .andExpect(jsonPath("$[1].provincia").value("Imbabura"))
                .andExpect(jsonPath("$[1].parroquia").value("Otavalo"))
                .andExpect(jsonPath("$[1].puntosComunidad").value(180));

        verify(rankingService).obtenerRankingComunidades();
    }

    @Test
    void retornaListaVaciaCuandoNoHayComunidadesEnRanking() throws Exception {
        when(rankingService.obtenerRankingComunidades()).thenReturn(List.of());

        mockMvc.perform(get("/api/publico/ranking/comunidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(rankingService).obtenerRankingComunidades();
    }

    @Test
    @DisplayName("GM-CU05: Obtiene ranking y medallero de voluntarios sin token JWT")
    void obtieneRankingDeMiembrosSinToken() throws Exception {
        RankingMiembroResponse m1 = new RankingMiembroResponse(
                1, 101L, "Carmen", "Chuquimarca", NivelMiembro.MAESTRO, 260, 4L);
        RankingMiembroResponse m2 = new RankingMiembroResponse(
                2, 102L, "Luis", "Guamán", NivelMiembro.OFICIAL, 130, 2L);

        when(rankingService.obtenerRankingMiembros()).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/publico/ranking/miembros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].posicion").value(1))
                .andExpect(jsonPath("$[0].miembroId").value(101))
                .andExpect(jsonPath("$[0].nombres").value("Carmen"))
                .andExpect(jsonPath("$[0].apellidos").value("Chuquimarca"))
                .andExpect(jsonPath("$[0].nivel").value("MAESTRO"))
                .andExpect(jsonPath("$[0].puntosTotales").value(260))
                .andExpect(jsonPath("$[0].totalInsignias").value(4))
                .andExpect(jsonPath("$[1].posicion").value(2))
                .andExpect(jsonPath("$[1].miembroId").value(102))
                .andExpect(jsonPath("$[1].nombres").value("Luis"))
                .andExpect(jsonPath("$[1].apellidos").value("Guamán"))
                .andExpect(jsonPath("$[1].nivel").value("OFICIAL"))
                .andExpect(jsonPath("$[1].puntosTotales").value(130))
                .andExpect(jsonPath("$[1].totalInsignias").value(2));

        verify(rankingService).obtenerRankingMiembros();
    }

    @Test
    @DisplayName("GM-CU05: Retorna lista vacía cuando no hay voluntarios registrados")
    void retornaListaVaciaCuandoNoHayMiembrosEnRanking() throws Exception {
        when(rankingService.obtenerRankingMiembros()).thenReturn(List.of());

        mockMvc.perform(get("/api/publico/ranking/miembros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(rankingService).obtenerRankingMiembros();
    }
}
