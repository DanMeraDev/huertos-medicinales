package com.huertos.service;

import com.huertos.dto.ActualizarMiembroRequest;
import com.huertos.dto.CambiarEstadoMiembroRequest;
import com.huertos.dto.FichaInternaMiembroResponse;
import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.MiembroEstadoResponse;
import com.huertos.dto.MiembroResumenResponse;
import com.huertos.dto.PerfilPublicoMiembroResponse;
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

    /**
     * Actualiza los datos de contacto y caracterización (género y teléfono) de un miembro de la comunidad.
     *
     * @param responsableId ID del usuario responsable de la comunidad
     * @param miembroId ID del miembro a actualizar
     * @param request Datos actualizados de contacto y género
     * @return Ficha detallada del miembro con los datos actualizados
     */
    MiembroDetalleResponse actualizarDatosContacto(Long responsableId, Long miembroId, ActualizarMiembroRequest request);

    /**
     * Actualiza los datos de contacto y caracterización de un miembro a partir del correo del responsable.
     *
     * @param correo Correo electrónico del usuario responsable de la comunidad
     * @param miembroId ID del miembro a actualizar
     * @param request Datos actualizados de contacto y género
     * @return Ficha detallada del miembro con los datos actualizados
     */
    MiembroDetalleResponse actualizarDatosContactoPorCorreo(String correo, Long miembroId, ActualizarMiembroRequest request);

    /**
     * Obtiene la ficha interna consolidada de un miembro perteneciente a la comunidad del responsable.
     *
     * @param responsableId ID del usuario responsable de la comunidad
     * @param miembroId ID del miembro a consultar
     * @return Ficha interna de solo lectura del miembro
     */
    FichaInternaMiembroResponse obtenerFichaInterna(Long responsableId, Long miembroId);

    /**
     * Obtiene la ficha interna consolidada de un miembro a partir del correo del responsable autenticado.
     *
     * @param correo Correo electrónico del usuario responsable de la comunidad
     * @param miembroId ID del miembro a consultar
     * @return Ficha interna de solo lectura del miembro
     */
    FichaInternaMiembroResponse obtenerFichaInternaPorCorreo(String correo, Long miembroId);

    /**
     * Lista los miembros activos de la comunidad del responsable autenticado.
     *
     * @param correo Correo electrónico del usuario responsable de la comunidad
     * @return Resumen de cada miembro activo con su nombre completo
     */
    List<MiembroResumenResponse> listarMiembrosActivosPorCorreo(String correo);

    /**
     * Obtiene el perfil público y motivacional de un miembro activo por su número de cédula,
     * omitiendo datos privados de contacto o fecha de nacimiento en cumplimiento de la LOPDP.
     *
     * @param cedula Número de cédula de 10 dígitos del miembro
     * @return Perfil público con nombre completo, rango, puntos, comunidades e insignias
     */
    PerfilPublicoMiembroResponse obtenerPerfilPublicoPorCedula(String cedula);
}
