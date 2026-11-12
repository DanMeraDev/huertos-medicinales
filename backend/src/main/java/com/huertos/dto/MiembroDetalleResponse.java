package com.huertos.dto;

import com.huertos.model.Genero;
import com.huertos.model.NivelMiembro;
import java.time.LocalDate;

public record MiembroDetalleResponse(
        Long id,
        Long comunidadId,
        String cedula,
        String nombreCompleto,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        Integer edad,
        Genero genero,
        String telefono,
        NivelMiembro nivel,
        Integer puntosTotales,
        boolean activoEnComunidad,
        LocalDate fechaIngreso
) {}
