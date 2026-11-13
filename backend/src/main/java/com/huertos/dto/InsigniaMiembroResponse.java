package com.huertos.dto;

import com.huertos.model.TipoOrigenInsignia;
import java.time.LocalDate;

public record InsigniaMiembroResponse(
        Long id,
        String nombreInsignia,
        String descripcion,
        String iconoCodigo,
        TipoOrigenInsignia tipoOrigen,
        LocalDate fechaObtencion
) {}
