package com.huertos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CambiarClaveRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String passwordActual,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{12,}$",
                message = "La contraseña debe tener al menos 12 caracteres, incluyendo al menos una mayúscula, una minúscula y un número")
        String nuevaPassword,

        @NotBlank(message = "La confirmación de la contraseña es obligatoria")
        String confirmarPassword) {
}
