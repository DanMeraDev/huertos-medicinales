package com.huertos.dto;

import java.time.Instant;

public record PingResponse(String estado, String baseDeDatos, Instant fecha) {
}
