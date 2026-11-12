package com.huertos.service;

import com.huertos.dto.ActividadCatalogoResponse;
import com.huertos.dto.CrearActividadCatalogoRequest;
import java.util.List;

public interface ActividadCatalogoService {

    ActividadCatalogoResponse crear(String correoResponsable, CrearActividadCatalogoRequest request);

    List<ActividadCatalogoResponse> listarPorResponsable(String correoResponsable);
}