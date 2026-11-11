package com.huertos.dto;

import com.huertos.model.Rol;

public record LoginResponse(
        String token,
        String tipoToken,
        Long id,
        String correo,
        String nombreCompleto,
        Rol rol,
        boolean requiereCambioClave) {
}
