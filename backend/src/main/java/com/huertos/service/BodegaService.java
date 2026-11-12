package com.huertos.service;

import com.huertos.dto.BajaMaterialResponse;
import com.huertos.dto.MaterialVegetalResponse;
import com.huertos.dto.RegistrarBajaMaterialRequest;
import com.huertos.dto.RegistrarMaterialVegetalRequest;
import java.util.List;
import org.springframework.security.core.Authentication;

/**
 * Servicio para la administración del material vegetal en las bodegas comunitarias.
 */
public interface BodegaService {

    /**
     * Registra la baja de una cantidad de material vegetal en bodega con su causa de fracaso.
     *
     * @param bodegaMaterialId identificador del lote en bodega
     * @param request datos de la baja a registrar
     * @param authentication datos del usuario autenticado
     * @return información de la baja registrada
     */
    BajaMaterialResponse registrarBajaMaterial(Long bodegaMaterialId, RegistrarBajaMaterialRequest request, Authentication authentication);

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
     * Lista el material vegetal activo disponible en la bodega de una comunidad.
     *
     * @param comunidadId identificador de la comunidad
     * @return lista de material vegetal disponible ordenada alfabéticamente por planta
     */
    List<MaterialVegetalResponse> listarMaterialPorComunidad(Long comunidadId);

    /**
     * Valida que el usuario autenticado tenga autorización para consultar la comunidad solicitada.
     *
     * @param comunidadId identificador de la comunidad solicitada
     * @param authentication credenciales del usuario autenticado
     */
    void validarAccesoComunidad(Long comunidadId, Authentication authentication);

    /**
     * Valida que el usuario tenga autorización para consultar la comunidad solicitada.
     *
     * @param comunidadId identificador de la comunidad solicitada
     * @param correo correo del usuario
     * @param esTecnico indica si el usuario posee rol técnico supervisor
     */
    void validarAccesoComunidad(Long comunidadId, String correo, boolean esTecnico);
}
