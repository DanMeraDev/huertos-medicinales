package com.huertos.dto;

import com.huertos.model.CausaFracaso;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BajaMaterialResponse(
        Long id,
        Long bodegaMaterialId,
        BigDecimal cantidadBaja,
        CausaFracaso causaFracaso,
        String observacion,
        LocalDateTime fechaBaja
) {}

