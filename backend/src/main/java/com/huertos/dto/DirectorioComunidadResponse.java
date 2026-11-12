package com.huertos.dto;

import java.math.BigDecimal;

public record DirectorioComunidadResponse(
        Long id,
        String nombre,
        String nombreResponsable,
        String telefonoContacto,
        String correoContacto,
        Integer totalMiembros,
        String provincia,
        String parroquia,
        String direccion,
        BigDecimal latitud,
        BigDecimal longitud,
        Integer altitudMsnm
) {}
