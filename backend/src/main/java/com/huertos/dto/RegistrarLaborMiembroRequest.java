package com.huertos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegistrarLaborMiembroRequest(
        @NotNull(message = "El voluntario es obligatorio")
        Long miembroId,

        @NotNull(message = "La actividad comunitaria es obligatoria")
        Long actividadComunidadId,

        @NotNull(message = "La fecha de la actividad es obligatoria")
        @PastOrPresent(message = "No se pueden registrar labores futuras")
        LocalDate fechaActividad,

        @NotNull(message = "Las horas trabajadas son obligatorias")
        @Min(value = 1, message = "La jornada mínima es de 1 hora")
        @Max(value = 24, message = "La jornada máxima permitida es de 24 horas por día según norma MSP")
        Integer horasTrabajadas,

        @Size(max = 255, message = "La observación no puede exceder 255 caracteres")
        String observacion
) {}
