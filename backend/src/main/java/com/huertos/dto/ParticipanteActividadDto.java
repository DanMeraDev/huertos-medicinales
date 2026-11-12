package com.huertos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ParticipanteActividadDto(
        @NotNull(message = "El miembro participante es obligatorio")
        Long miembroId,

        @NotNull(message = "Las horas trabajadas son obligatorias")
        @Min(value = 1, message = "Las horas trabajadas deben ser al menos 1")
        @Max(value = 24, message = "Las horas trabajadas no pueden superar 24")
        Integer horasTrabajadas
) {}
