package com.huertos.dto;

import com.huertos.model.CategoriaActividad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearActividadCatalogoRequest(
        @NotBlank(message = "El nombre de la actividad es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombre,
        @NotNull(message = "La categoría es obligatoria")
        CategoriaActividad categoria
) {}