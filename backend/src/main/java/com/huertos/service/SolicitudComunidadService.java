package com.huertos.service;

import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.SolicitudComunidadResponse;

public interface SolicitudComunidadService {

    /**
     * Registra una solicitud pública en estado PENDIENTE y guarda la fecha y hora del consentimiento LOPDP.
     *
     * @throws com.huertos.exception.ReglaNegocioException si el solicitante es menor de 18 años
     * @throws com.huertos.exception.ConflictoException si el nombre o la cédula ya tienen una solicitud pendiente
     *         o una comunidad registrada
     */
    SolicitudComunidadResponse registrarSolicitud(CrearSolicitudComunidadRequest request);
}
