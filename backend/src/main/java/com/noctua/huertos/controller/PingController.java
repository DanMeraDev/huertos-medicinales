package com.noctua.huertos.controller;

import com.noctua.huertos.dto.PingResponse;
import com.noctua.huertos.service.PingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ping")
@RequiredArgsConstructor
public class PingController {

    private final PingService pingService;

    @GetMapping
    public PingResponse ping() {
        return pingService.ping();
    }
}
