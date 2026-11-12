package com.huertos.dto;

import com.huertos.model.CategoriaUso;
import com.huertos.model.CicloCultivo;
import com.huertos.model.CualidadTermica;
import com.huertos.model.OrigenPlanta;

public record FiltrosPlantaRequest(
        CualidadTermica cualidad,
        Integer altitud,
        String region,
        CategoriaUso categoria,
        CicloCultivo ciclo,
        OrigenPlanta origen,
        String criterio) {
}
