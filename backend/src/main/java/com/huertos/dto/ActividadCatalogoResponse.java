package com.huertos.dto;

import com.huertos.model.CategoriaActividad;

public record ActividadCatalogoResponse(
        Long id,
        String nombre,
        CategoriaActividad categoria,
        Integer puntosPorHora,
        boolean activa
) {}