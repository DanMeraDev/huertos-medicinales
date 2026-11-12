package com.huertos.dto;

import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CrearPlantaRequest(
        @NotBlank(message = "El nombre común es obligatorio")
        @Size(max = 120, message = "El nombre común no puede superar los 120 caracteres")
        String nombreComun,

        @Size(max = 120, message = "El nombre kichwa no puede superar los 120 caracteres")
        String nombreKichwa,

        @NotBlank(message = "El nombre científico es obligatorio")
        @Size(max = 150, message = "El nombre científico no puede superar los 150 caracteres")
        String nombreCientifico,

        @NotBlank(message = "La familia botánica es obligatoria")
        @Size(max = 100, message = "La familia botánica no puede superar los 100 caracteres")
        String familiaBotanica,

        @NotNull(message = "La cualidad térmica es obligatoria")
        CualidadTermica cualidadTermica,

        CicloCultivo cicloCultivo,

        OrigenPlanta origen,

        @PositiveOrZero(message = "La altitud mínima no puede ser negativa")
        Integer altitudMinMsnm,

        @PositiveOrZero(message = "La altitud máxima no puede ser negativa")
        Integer altitudMaxMsnm,

        @Size(max = 150, message = "Las regiones de distribución no pueden superar los 150 caracteres")
        String regionesDistribucion,

        @NotBlank(message = "Los usos medicinales son obligatorios")
        String usosMedicinales,

        String preparacionDosificacion,

        String contraindicaciones,

        @NotEmpty(message = "Seleccione al menos una categoría de uso")
        Set<CategoriaUso> categorias,

        @Size(max = 500, message = "La ruta de la fotografía no puede superar los 500 caracteres")
        String fotografiaUrl) {
}
