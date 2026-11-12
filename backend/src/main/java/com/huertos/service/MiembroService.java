package com.huertos.service;

import com.huertos.dto.CambiarEstadoMiembroRequest;
import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.MiembroEstadoResponse;
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
     * Actualiza el estado de participación (alta o baja) de un miembro en la comunidad del responsable.
     *
     * @param responsableId ID del usuario responsable de la comunidad
     * @param miembroId ID del miembro a modificar
     * @param request Nuevo estado y motivo opcional si es baja
     * @return Confirmación del cambio de estado del miembro
     */
    MiembroEstadoResponse cambiarEstadoEnComunidad(Long responsableId, Long miembroId, CambiarEstadoMiembroRequest request);

    /**
     * Actualiza el estado de participación (alta o baja) de un miembro a partir del correo del responsable.
     *
     * @param correo Correo electrónico del usuario responsable de la comunidad
     * @param miembroId ID del miembro a modificar
     * @param request Nuevo estado y motivo opcional si es baja
     * @return Confirmación del cambio de estado del miembro
     */
    MiembroEstadoResponse cambiarEstadoPorCorreo(String correo, Long miembroId, CambiarEstadoMiembroRequest request);

    /**
     * Lista los miembros vinculados a la comunidad liderada por el responsable.
     *
     * @param responsableId ID del usuario responsable de la comunidad
     * @return Listado de miembros de la comunidad
     */
    List<MiembroDetalleResponse> listarMiembrosDeMiComunidad(Long responsableId);

    /**
     * Lista los miembros vinculados a la comunidad a partir del correo del responsable.
     *
     * @param correo Correo electrónico del usuario responsable de la comunidad
     * @return Listado de miembros de la comunidad
     */
    List<MiembroDetalleResponse> listarMiembrosPorCorreo(String correo);
}
