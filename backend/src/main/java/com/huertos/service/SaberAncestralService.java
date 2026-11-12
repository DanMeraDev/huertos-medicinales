package com.huertos.service;

import com.huertos.dto.SaberAncestralDetalleResponse;
import com.huertos.dto.SaberAncestralResumenResponse;
import java.util.List;

public interface SaberAncestralService {

    /**
     * Devuelve la ruta secuencial de saberes ancestrales ordenada por paso.
     */
    List<SaberAncestralResumenResponse> listarRutaSaberes();

    /**
     * Devuelve el contenido pedagógico completo de un saber ancestral.
     *
     * @throws com.huertos.exception.RecursoNoEncontradoException si no existe el saber solicitado
     */
    SaberAncestralDetalleResponse obtenerPorId(Long id);
}
