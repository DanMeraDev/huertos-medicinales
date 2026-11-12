package com.huertos.service;

import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarMaterialVegetalRequest;

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
}
