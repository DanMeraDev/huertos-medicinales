package com.huertos.service;

import com.huertos.dto.ComunidadResponse;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.Usuario;

public interface ComunidadService {

    /**
     * Crea la comunidad de una solicitud aprobada y su huerto base. El huerto toma el nombre de la
     * comunidad y queda sin área ni subhuertos hasta que se configuren en HR-CU03 y HR-CU04.
     *
     * @throws com.huertos.exception.ReglaNegocioException si el responsable no está activo
     * @throws com.huertos.exception.ConflictoException si ya existe una comunidad con ese nombre
     */
    ComunidadResponse registrarComunidadDesdeSolicitud(SolicitudComunidad solicitud, Usuario responsable);
}
