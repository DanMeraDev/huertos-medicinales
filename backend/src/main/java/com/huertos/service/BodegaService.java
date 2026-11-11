package com.huertos.service;

import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import java.util.List;

/**
 * Servicio para la administración del material vegetal en las bodegas comunitarias.
 */
public interface BodegaService {

    /**
     * Registra el ingreso de nuevo material vegetal a la bodega de la comunidad.
     *
     * @param comunidadId identificador de la comunidad
     * @param request datos del material vegetal a registrar
     * @return información del material vegetal registrado
     */
    MaterialVegetalResponse registrarIngresoMaterial(Long comunidadId, RegistrarMaterialVegetalRequest request);

    /**
     * Obtiene el identificador de la comunidad asignada al usuario responsable según su correo.
     *
     * @param correo correo electrónico del usuario responsable
     * @return identificador de la comunidad
     */
    Long obtenerComunidadIdPorCorreo(String correo);

    /**
     * Lista el material vegetal disponible en la bodega de una comunidad.
     *
     * @param comunidadId identificador de la comunidad a consultar
     * @return lista de lotes de material vegetal disponibles
     */
    List<MaterialVegetalResponse> listarMaterialPorComunidad(Long comunidadId);

    /**
     * Lista el material vegetal de una comunidad validando permisos del usuario.
     *
     * @param comunidadId identificador de la comunidad a consultar
     * @param correoUsuario correo del usuario autenticado
     * @param esTecnicoMsp indica si el usuario tiene rol de técnico MSP
     * @return lista de lotes de material vegetal disponibles
     */
    List<MaterialVegetalResponse> listarMaterialPorComunidad(Long comunidadId, String correoUsuario, boolean esTecnicoMsp);
}

