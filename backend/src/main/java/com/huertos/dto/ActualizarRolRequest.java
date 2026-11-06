package com.huertos.dto;

import com.huertos.model.Rol;
import jakarta.validation.constraints.NotNull;

public record ActualizarRolRequest(@NotNull Rol nuevoRol) {
}
