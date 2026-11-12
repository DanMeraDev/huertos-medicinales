package com.huertos.controller;

import com.huertos.dto.CalendarioLunarResponse;
import com.huertos.service.CalendarioLunarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Calendario lunar")
@RestController
@RequestMapping("/api/publico/calendario-lunar")
@RequiredArgsConstructor
public class CalendarioLunarPublicoController {

    private final CalendarioLunarService calendarioLunarService;
    private final Clock clock;

    @Operation(summary = "Obtiene la fase lunar vigente")
    @GetMapping("/actual")
    public CalendarioLunarResponse obtenerFaseVigente() {
        return calendarioLunarService.obtenerFaseActual(LocalDate.now(clock));
    }

    @Operation(summary = "Obtiene la fase lunar de una fecha")
    @GetMapping
    public CalendarioLunarResponse obtenerFasePorFecha(@RequestParam LocalDate fecha) {
        return calendarioLunarService.obtenerFaseActual(fecha);
    }
}
