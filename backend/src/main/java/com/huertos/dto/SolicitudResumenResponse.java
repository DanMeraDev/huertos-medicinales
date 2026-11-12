package com.huertos.dto;

import java.time.LocalDate;

public record SolicitudResumenResponse(Long id, String solicitante, LocalDate fecha, String provincia) {
}
