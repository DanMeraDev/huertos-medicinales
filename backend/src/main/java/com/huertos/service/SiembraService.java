package com.huertos.service;

import com.huertos.dto.RegistrarSiembraRequest;
import com.huertos.dto.SiembraDetalleResponse;

/**
 * Servicio para la gestión de siembras en subhuertos comunitarios.
 */
public interface SiembraService {

    /**
     * Registra una nueva siembra en un subhuerto validando la pertenencia comunitaria,
     * la disponibilidad de stock en bodega y la concordancia de altitud de la planta.
     *
     * @param correoResponsable correo del responsable de la comunidad autenticado
     * @param request datos para el registro de la siembra
     * @return detalle de la siembra registrada
     */
    SiembraDetalleResponse registrarSiembra(String correoResponsable, RegistrarSiembraRequest request);
}

