package com.huertos.dto;

import com.huertos.model.Genero;
import com.huertos.model.NivelMiembro;
import java.time.LocalDate;

public record FichaInternaMiembroResponse(
        Long id,
        Long comunidadId,
        String nombreComunidad,
        String cedula,
        String nombres,
        String apellidos,
        String nombreCompleto,
        LocalDate fechaNacimiento,
        Integer edad,
        Genero genero,
        String telefono,
        NivelMiembro nivel,
        Integer puntosTotales,
        boolean activoEnComunidad,
        LocalDate fechaIngreso,
        LocalDate fechaBaja,
        String motivoBaja
) {}
