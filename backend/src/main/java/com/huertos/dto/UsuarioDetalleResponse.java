package com.huertos.dto;

import com.huertos.model.Genero;
import com.huertos.model.Rol;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioDetalleResponse(
        Long id,
        String nombreCompleto,
        String identificacion,
        LocalDate fechaNacimiento,
        Genero genero,
        String correo,
        String telefono,
        Rol rol,
        boolean activo,
        boolean requiereCambioClave,
        LocalDateTime creadoEn) {
}
