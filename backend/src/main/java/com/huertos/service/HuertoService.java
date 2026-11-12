package com.huertos.service;

import com.huertos.dto.ActualizarHuertoRequest;
import com.huertos.dto.HuertoDetalleResponse;

public interface HuertoService {

    /**
     * Obtiene la configuración operativa del huerto de la comunidad asociada al correo del usuario autenticado.
     *
     * @param correo correo del responsable o técnico MSP autenticado
     * @return detalle completo del huerto con el área ocupada por subhuertos
     * @throws com.huertos.exception.RecursoNoEncontradoException si no existe usuario o huerto asociado
     */
    HuertoDetalleResponse obtenerPorCorreo(String correo);

    /**
     * Actualiza la configuración agronómica, territorial y física del huerto.
     * El área total no puede reducirse por debajo de la suma de áreas de los subhuertos activos.
     *
     * @param correo  correo del responsable autenticado
     * @param request datos operativos a actualizar
     * @return detalle del huerto con los datos actualizados
     * @throws com.huertos.exception.RecursoNoEncontradoException si no existe usuario o huerto asociado
     * @throws com.huertos.exception.ReglaNegocioException        si el área es menor a la ocupada por subhuertos
     */
    HuertoDetalleResponse actualizarPorCorreo(String correo, ActualizarHuertoRequest request);
}

