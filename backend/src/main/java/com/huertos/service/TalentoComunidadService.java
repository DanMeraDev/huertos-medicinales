package com.huertos.service;

import com.huertos.dto.CrearTalentoComunidadRequest;
import com.huertos.dto.TalentoComunidadResponse;
import java.util.List;

public interface TalentoComunidadService {

    /**
     * Lista los talentos o medallas activas configuradas en la comunidad del responsable.
     *
     * @param responsableId ID del usuario responsable de la comunidad
     * @return Listado de talentos de la comunidad
     */
    List<TalentoComunidadResponse> listarPorResponsable(Long responsableId);

    /**
     * Lista los talentos o medallas activas a partir del correo del responsable autenticado.
     *
     * @param correo Correo electrónico del usuario responsable
     * @return Listado de talentos de la comunidad
     */
    List<TalentoComunidadResponse> listarPorCorreo(String correo);

    /**
     * Registra un nuevo talento o insignia personalizada en la comunidad del responsable.
     *
     * @param responsableId ID del usuario responsable de la comunidad
     * @param request Datos del nuevo talento a registrar
     * @return Detalle del talento creado
     */
    TalentoComunidadResponse registrarTalento(Long responsableId, CrearTalentoComunidadRequest request);

    /**
     * Registra un nuevo talento o insignia personalizada a partir del correo del responsable autenticado.
     *
     * @param correo Correo electrónico del usuario responsable
     * @param request Datos del nuevo talento a registrar
     * @return Detalle del talento creado
     */
    TalentoComunidadResponse registrarTalentoPorCorreo(String correo, CrearTalentoComunidadRequest request);
}
