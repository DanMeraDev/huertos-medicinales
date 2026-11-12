package com.huertos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ReenviarEvidenciasRequest(
        @NotBlank(message = "La descripción de la bitácora es obligatoria")
        String descripcionBitacora,

        @NotBlank(message = "La fotografía de evidencia es obligatoria para la revisión del MSP")
        @Size(max = 500, message = "La ruta de la fotografía no puede superar 500 caracteres")
        String evidenciaFotoUrl,

        @Pattern(regexp = "^https?://\\S+$", message = "El video de evidencia debe ser una URL válida")
        @Size(max = 500, message = "La URL del video no puede superar 500 caracteres")
        String evidenciaVideoUrl
) {}
