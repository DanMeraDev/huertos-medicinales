package com.huertos.service;

import com.huertos.dto.ActividadAncestralResponse;
import com.huertos.dto.ActividadParaCorregirResponse;
import com.huertos.dto.RegistrarActividadAncestralRequest;
import com.huertos.dto.ReenviarEvidenciasRequest;
import java.util.List;

public interface ActividadAncestralService {

    /**
     * Registra una actividad ancestral grande (hito oficial de la ruta MSP) con evidencias
     * y nómina de participantes; queda en estado PENDIENTE para la revisión del Técnico MSP.
     *
     * @param correo Correo electrónico del responsable de comunidad autenticado
     * @param request Datos de la actividad, evidencias y participantes con sus horas trabajadas
     * @return Actividad registrada con su estado inicial PENDIENTE y número de intento 1
     */
    ActividadAncestralResponse registrarActividadGrande(String correo, RegistrarActividadAncestralRequest request);

    /**
     * Obtiene la actividad ancestral observada con la observación del Técnico MSP, para que el
     * responsable corrija la bitácora y las evidencias.
     *
     * @param correo Correo electrónico del responsable de comunidad autenticado
     * @param id Identificador de la actividad ancestral
     * @return Actividad con su observación, número de intento y evidencias actuales
     */
    ActividadParaCorregirResponse buscarParaCorregir(String correo, Long id);

    /**
     * Lista las actividades de la comunidad del responsable que están en estado OBSERVADA_CORREGIR,
     * con la observación del Técnico MSP, para que el responsable las corrija y reenvíe.
     *
     * @param correo Correo electrónico del responsable de comunidad autenticado
     * @return Actividades observadas de su comunidad pendientes de corrección
     */
    List<ActividadParaCorregirResponse> listarObservadas(String correo);

    /**
     * Reenvía las evidencias corregidas de una actividad observada: la devuelve a estado PENDIENTE
     * para una nueva revisión del Técnico MSP e incrementa el número de intento. La normativa técnica
     * del MSP admite como máximo dos intentos, así que un tercer reenvío se rechaza.
     *
     * @param correo Correo electrónico del responsable de comunidad autenticado
     * @param id Identificador de la actividad ancestral
     * @param request Bitácora y evidencias corregidas
     * @return Actividad reenviada con su estado PENDIENTE y el número de intento incrementado
     */
    ActividadAncestralResponse reenviarEvidencias(String correo, Long id, ReenviarEvidenciasRequest request);
}
