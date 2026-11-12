package com.huertos.dto;

import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;
import java.util.Set;

public record CrearPlantaRequest(
        String nombreComun,
        String nombreKichwa,
        String nombreCientifico,
        String familiaBotanica,
        CualidadTermica cualidadTermica,
        CicloCultivo cicloCultivo,
        OrigenPlanta origen,
        Integer altitudMinMsnm,
        Integer altitudMaxMsnm,
        String regionesDistribucion,
        String usosMedicinales,
        String preparacionDosificacion,
        String contraindicaciones,
        Set<CategoriaUso> categorias,
        String fotografiaUrl) {
}
