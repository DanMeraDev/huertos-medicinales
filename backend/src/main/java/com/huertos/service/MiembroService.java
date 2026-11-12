package com.huertos.service;

import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.MiembroResumenResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import java.util.List;

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

    /**
     * Lista los miembros activos de la comunidad del responsable autenticado.
     *
     * @param correo Correo electrónico del usuario responsable de la comunidad
     * @return Resumen de cada miembro activo con su nombre completo
     */
    List<MiembroResumenResponse> listarMiembrosActivosPorCorreo(String correo);
}
