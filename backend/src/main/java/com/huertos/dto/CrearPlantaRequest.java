package com.huertos.dto;

import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearPlantaRequest(
        @NotBlank(message = CrearPlantaRequest.OBLIGATORIO)
        @Size(max = 100, message = CrearPlantaRequest.MAXIMO) String nombreComun,
        @NotBlank(message = CrearPlantaRequest.OBLIGATORIO)
        @Size(max = 120, message = CrearPlantaRequest.MAXIMO) String nombreCientifico,
        @Size(max = 100, message = CrearPlantaRequest.MAXIMO) String nombreKichwa,
        @NotBlank(message = CrearPlantaRequest.OBLIGATORIO)
        @Size(max = 80, message = CrearPlantaRequest.MAXIMO) String familiaBotanica,
        @NotBlank(message = CrearPlantaRequest.OBLIGATORIO)
        @Size(max = 100, message = CrearPlantaRequest.MAXIMO) String regionDistribucion,
        @Size(max = 60, message = CrearPlantaRequest.MAXIMO) String altitudMsnm,
        @NotNull(message = CrearPlantaRequest.OBLIGATORIO) CicloCultivo cicloCultivo,
        @NotNull(message = CrearPlantaRequest.OBLIGATORIO) CualidadTermica cualidadTermica,
        @NotBlank(message = CrearPlantaRequest.OBLIGATORIO)
        @Size(max = 2000, message = CrearPlantaRequest.MAXIMO) String usosMedicinales,
        @Size(max = 2000, message = CrearPlantaRequest.MAXIMO) String indicacionesDosificacion,
        @Size(max = 2000, message = CrearPlantaRequest.MAXIMO) String contraindicaciones) {

    static final String OBLIGATORIO = "Este campo es obligatorio";
    static final String MAXIMO = "Máximo {max} caracteres";
}
