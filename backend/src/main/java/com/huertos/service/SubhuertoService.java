package com.huertos.service;

import com.huertos.dto.CrearSubhuertoRequest;
import com.huertos.dto.SubhuertoResponse;
import java.util.List;

public interface SubhuertoService {

    SubhuertoResponse crear(String correoResponsable, CrearSubhuertoRequest request);

    List<SubhuertoResponse> listarPorUsuario(String correoUsuario);
}