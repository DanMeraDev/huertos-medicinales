package com.huertos.service;

import com.huertos.dto.CredencialesEmailDto;
import com.huertos.dto.RechazoSolicitudEmailDto;

public interface EmailService {

    /**
     * Envía al responsable aprobado su usuario, la contraseña temporal y el enlace al inicio de sesión.
     * Se ejecuta en otro hilo después de confirmar la aprobación; si el servidor de correo falla,
     * solo se registra en el log y la aprobación se mantiene.
     */
    void enviarCredencialesAprobacion(CredencialesEmailDto datos);

    /**
     * Envía al solicitante el motivo del rechazo. Se ejecuta en otro hilo después de confirmar el rechazo;
     * si el servidor de correo falla, solo se registra en el log.
     */
    void enviarNotificacionRechazo(RechazoSolicitudEmailDto datos);
}
