package com.huertos.controller;

import com.huertos.dto.RankingComunidadResponse;
import com.huertos.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico/ranking")
@RequiredArgsConstructor
@Tag(name = "Ranking público de comunidades")
public class RankingPublicoController {

    private final RankingService rankingService;

    @GetMapping("/comunidades")
    @Operation(summary = "Obtiene la tabla de posiciones y ranking general de comunidades de huertos medicinales")
    public List<RankingComunidadResponse> obtenerRankingComunidades() {
        return rankingService.obtenerRankingComunidades();
    }
}
