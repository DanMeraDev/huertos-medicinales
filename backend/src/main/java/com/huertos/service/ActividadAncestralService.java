package com.huertos.service;

import com.huertos.dto.ActividadAncestralResponse;
import com.huertos.dto.RegistrarActividadAncestralRequest;

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
}
