package com.huertos.service;

import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.RegistrarMiembroRequest;

public interface MiembroService {

    /**
     * Registra un nuevo miembro voluntario en la comunidad del responsable o vincula uno existente.
     *
     * @param responsableId ID del usuario responsable de la comunidad
     * @param request Datos del miembro y certificación de consentimiento LOPDP
     * @return Ficha detallada del miembro y su membresía
     */
    MiembroDetalleResponse registrarMiembroEnComunidad(Long responsableId, RegistrarMiembroRequest request);

    /**
     * Registra un nuevo miembro voluntario en la comunidad a partir del correo del responsable autenticado.
     *
     * @param correo Correo electrónico del usuario responsable de la comunidad
     * @param request Datos del miembro y certificación de consentimiento LOPDP
     * @return Ficha detallada del miembro y su membresía
     */
    MiembroDetalleResponse registrarMiembroPorCorreo(String correo, RegistrarMiembroRequest request);
}
