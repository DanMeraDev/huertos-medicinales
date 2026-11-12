package com.huertos.service;

import com.huertos.dto.ActualizarComunidadRequest;
import com.huertos.dto.ComunidadDetalleResponse;
import com.huertos.dto.ComunidadResponse;
import com.huertos.dto.DirectorioComunidadResponse;
import com.huertos.model.SolicitudComunidad;
import com.huertos.model.Usuario;
import java.util.List;

public interface ComunidadService {

    /**
     * Crea la comunidad de una solicitud aprobada y su huerto base. El huerto toma el nombre de la
     * comunidad y queda sin área ni subhuertos hasta que se configuren en HR-CU03 y HR-CU04.
     *
     * @param solicitud solicitud comunitaria aprobada
     * @param responsable usuario responsable activo asignado
     * @return datos de la comunidad registrada
     * @throws com.huertos.exception.ReglaNegocioException si el responsable no está activo
     * @throws com.huertos.exception.ConflictoException si ya existe una comunidad con ese nombre
     */
    ComunidadResponse registrarComunidadDesdeSolicitud(SolicitudComunidad solicitud, Usuario responsable);

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

    /**
     * Obtiene el directorio público consolidado de comunidades activas con su responsable,
     * cantidad de miembros activos y datos territoriales del huerto base.
     *
     * @return lista de comunidades activas para consulta pública
     */
    List<DirectorioComunidadResponse> obtenerDirectorioPublico();
}
