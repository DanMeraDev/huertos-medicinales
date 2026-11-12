package com.huertos.service;

import com.huertos.dto.ActualizarComunidadRequest;
import com.huertos.dto.ComunidadDetalleResponse;

public interface ComunidadService {

    /**
     * Obtiene la ficha de la comunidad asociada al responsable indicado.
     *
     * @param responsableId identificador del usuario responsable
     * @return detalle de la comunidad encontrada
     * @throws com.huertos.exception.RecursoNoEncontradoException si el responsable no tiene comunidad asignada
     */
    ComunidadDetalleResponse obtenerPorResponsableId(Long responsableId);

    /**
     * Actualiza el nombre y los canales oficiales de contacto de la comunidad del responsable.
     *
     * @param responsableId identificador del usuario responsable
     * @param request datos a actualizar (nombre, teléfono y correo)
     * @return detalle de la comunidad con los datos actualizados
     * @throws com.huertos.exception.RecursoNoEncontradoException si el responsable no tiene comunidad asignada
     * @throws com.huertos.exception.ConflictoException si el nombre ya está registrado por otra comunidad
     */
    ComunidadDetalleResponse actualizarInformacionContacto(Long responsableId, ActualizarComunidadRequest request);

    /**
     * Obtiene la ficha de la comunidad asociada al correo del responsable autenticado.
     *
     * @param correo correo del responsable
     * @return detalle de la comunidad encontrada
     * @throws com.huertos.exception.RecursoNoEncontradoException si no existe usuario o comunidad asociada
     */
    ComunidadDetalleResponse obtenerPorCorreo(String correo);

    /**
     * Actualiza la información de contacto de la comunidad asociada al correo del responsable autenticado.
     *
     * @param correo correo del responsable
     * @param request datos a actualizar
     * @return detalle de la comunidad con los datos actualizados
     * @throws com.huertos.exception.RecursoNoEncontradoException si no existe usuario o comunidad asociada
     * @throws com.huertos.exception.ConflictoException si el nombre ya está registrado por otra comunidad
     */
    ComunidadDetalleResponse actualizarPorCorreo(String correo, ActualizarComunidadRequest request);
}
