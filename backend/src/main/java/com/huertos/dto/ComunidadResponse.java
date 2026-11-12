package com.huertos.dto;

import com.huertos.model.TipoOrganizacion;
import java.time.LocalDateTime;

public record ComunidadResponse(
        Long id,
        String nombre,
        TipoOrganizacion tipoOrganizacion,
        String provincia,
        String parroquia,
        String direccion,
        String telefonoContacto,
        String correoContacto,
        Long responsableId,
        Integer puntosComunidad,
        boolean activa,
        LocalDateTime creadoEn) {
}
