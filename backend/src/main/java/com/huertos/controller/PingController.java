package com.huertos.controller;

import com.huertos.dto.PingResponse;
import com.huertos.service.PingService;
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
