package com.huertos.dto;

import com.huertos.model.Rol;
import java.util.List;

public record ContextoBodegaResponse(Rol rol, List<ComunidadResumenResponse> comunidades) {}
