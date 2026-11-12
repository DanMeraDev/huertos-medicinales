package com.huertos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearTalentoComunidadRequest(
        @NotNull(message = "La actividad es obligatoria")
        Long actividadComunidadId,

        @NotBlank(message = "El nombre de la insignia es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$", message = "El nombre de la insignia solo puede contener letras, números y espacios")
        @Size(max = 120, message = "El nombre de la insignia no puede superar 120 caracteres")
        String nombreInsignia,

        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion,

        @NotNull(message = "La meta es obligatoria")
        @Min(value = 1, message = "La meta de repeticiones debe ser un número entero mayor a 0")
        Integer metaRepeticiones
) {}
