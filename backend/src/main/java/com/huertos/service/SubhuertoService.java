package com.huertos.service;

import com.huertos.dto.CrearSubhuertoRequest;
import com.huertos.dto.SubhuertoResponse;
import java.util.List;

/** Gestiona la zonificación de subhuertos de una comunidad (HR-CU04). */
public interface SubhuertoService {

    /**
     * Crea un subhuerto activo validando que el huerto tenga área registrada, no supere
     * el límite de 3 subtipos distintos exigido por la norma MSP y no exceda el área disponible.
     */
    SubhuertoResponse crear(String correoResponsable, CrearSubhuertoRequest request);

    /** Lista los subhuertos activos del huerto asociado al usuario autenticado. */
    List<SubhuertoResponse> listarPorUsuario(String correoUsuario);
}