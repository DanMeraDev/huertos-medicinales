package com.huertos.dto;

import com.huertos.model.CategoriaUso;
import com.huertos.model.CualidadTermica;
import java.util.Set;

public record PlantaResumenResponse(
        Long id,
        String nombreComun,
        String nombreKichwa,
        String nombreCientifico,
        CualidadTermica cualidadTermica,
        Set<CategoriaUso> categorias,
        String fotografiaUrl) {
}
