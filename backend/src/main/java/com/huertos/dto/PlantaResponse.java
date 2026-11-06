package com.huertos.dto;

import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;

public record PlantaResponse(
        Long id,
        String nombreComun,
        String nombreCientifico,
        String nombreKichwa,
        String familiaBotanica,
        String regionDistribucion,
        String altitudMsnm,
        CicloCultivo cicloCultivo,
        CualidadTermica cualidadTermica,
        String usosMedicinales,
        String indicacionesDosificacion,
        String contraindicaciones,
        String fotoUrl) {
}
