package com.huertos.controller;

import com.huertos.dto.RankingComunidadResponse;
import com.huertos.service.RankingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico/ranking")
@RequiredArgsConstructor
public class RankingPublicoController {

    private final RankingService rankingService;

    @GetMapping("/comunidades")
    public List<RankingComunidadResponse> obtenerRankingComunidades() {
        throw new UnsupportedOperationException("Pendiente de implementacion");
    }
}
