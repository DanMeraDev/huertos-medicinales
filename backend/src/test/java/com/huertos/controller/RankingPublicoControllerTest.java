package com.huertos.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.huertos.config.SecurityConfig;
import com.huertos.dto.RankingComunidadResponse;
import com.huertos.model.TipoOrganizacion;
import com.huertos.service.RankingService;
import java.util.List;
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
}
