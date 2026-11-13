package com.huertos.dto;

import com.huertos.model.FaseLunar;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record RegistrarActividadAncestralRequest(
        @NotNull(message = "El saber ancestral es obligatorio")
        Long saberAncestralId,

        @NotNull(message = "La fecha de realización es obligatoria")
        LocalDate fechaRealizacion,

        @NotNull(message = "La fase lunar de ejecución es obligatoria")
        FaseLunar faseLunarEjecucion,

        @NotBlank(message = "La descripción de la bitácora es obligatoria")
        String descripcionBitacora,

        @NotBlank(message = "La fotografía de evidencia es obligatoria para la revisión del MSP")
        @Size(max = 500, message = "La ruta de la fotografía no puede superar 500 caracteres")
        @Pattern(regexp = "^$|^actividades/[a-z0-9-]+\\.(jpg|png|webp)$",
                message = "La fotografía de evidencia debe subirse desde el formulario de la actividad")
        String evidenciaFotoUrl,

        @Pattern(regexp = "^https?://\\S+$", message = "El video de evidencia debe ser una URL válida")
        @Size(max = 500, message = "La URL del video no puede superar 500 caracteres")
        String evidenciaVideoUrl,

        @NotEmpty(message = "Debe registrar al menos un miembro participante en la actividad ancestral")
        List<@Valid ParticipanteActividadDto> participantes
) {}
