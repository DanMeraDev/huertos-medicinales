package com.huertos.dto;

import com.huertos.model.SubtipoSubhuerto;
import java.math.BigDecimal;

public record SubhuertoResponse(
        Long id,
        Long huertoId,
        String nombreZona,
        SubtipoSubhuerto subtipo,
        BigDecimal areaM2,
        boolean activo
) {}