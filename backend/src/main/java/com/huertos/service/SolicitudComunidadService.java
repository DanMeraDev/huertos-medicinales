package com.huertos.service;

import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.RechazarSolicitudRequest;
import com.huertos.dto.ResolucionSolicitudResponse;
import com.huertos.dto.SolicitudComunidadResponse;
import com.huertos.dto.SolicitudDetalleCompletoResponse;
import com.huertos.dto.SolicitudResumenResponse;
import com.huertos.model.EstadoSolicitud;
import java.util.List;

public interface SolicitudComunidadService {

    /**
     * Registra una solicitud pública en estado PENDIENTE y guarda la fecha y hora del consentimiento LOPDP.
     *
     * @throws com.huertos.exception.ReglaNegocioException si el solicitante es menor de 18 años
     * @throws com.huertos.exception.ConflictoException si el nombre o la cédula ya tienen una solicitud pendiente
     *         o una comunidad registrada
     */
    SolicitudComunidadResponse registrarSolicitud(CrearSolicitudComunidadRequest request);

    /**
     * Lista las solicitudes en el estado indicado, de la más antigua a la más reciente.
     */
    List<SolicitudResumenResponse> listarPorEstado(EstadoSolicitud estado);

    /**
     * Obtiene la ficha completa de una solicitud.
     *
     * @throws com.huertos.exception.RecursoNoEncontradoException si la solicitud no existe
     */
    SolicitudDetalleCompletoResponse obtenerPorId(Long id);

    /**
     * Aprueba una solicitud pendiente en una sola transacción: crea la cuenta del responsable con una
     * contraseña temporal aleatoria (US-CU03), la comunidad con su huerto base (US-CU06) y registra al técnico
     * que la revisó. Si alguna parte falla, no se guarda nada. Al confirmarse, envía las credenciales por correo (US-CU07).
     *
     * @throws com.huertos.exception.RecursoNoEncontradoException si la solicitud o el técnico no existen
     * @throws com.huertos.exception.ConflictoException si la solicitud ya fue resuelta
     */
    ResolucionSolicitudResponse aprobarSolicitud(Long id, String correoTecnico);

    /**
     * Rechaza una solicitud pendiente guardando el motivo y el técnico que la revisó. Al confirmarse, envía el
     * motivo por correo al solicitante (US-CU07).
     *
     * @throws com.huertos.exception.RecursoNoEncontradoException si la solicitud o el técnico no existen
     * @throws com.huertos.exception.ConflictoException si la solicitud ya fue resuelta
     */
    ResolucionSolicitudResponse rechazarSolicitud(Long id, RechazarSolicitudRequest request, String correoTecnico);
}
