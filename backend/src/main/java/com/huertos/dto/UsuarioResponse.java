package com.huertos.dto;

import com.huertos.model.Rol;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombreCompleto,
        String identificacion,
        String email,
        Rol rol,
        boolean activo,
        LocalDateTime creadoEn) {
}
