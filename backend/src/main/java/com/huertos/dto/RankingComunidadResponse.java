package com.huertos.dto;

import com.huertos.model.TipoOrganizacion;

public record RankingComunidadResponse(
        Integer posicion,
        Long comunidadId,
        String nombreComunidad,
        TipoOrganizacion tipoOrganizacion,
        String provincia,
        String parroquia,
        Integer puntosComunidad) {
}
