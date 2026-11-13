package com.huertos.service;

import com.huertos.dto.ActividadRevisionResponse;
import com.huertos.dto.ResultadoVerificacionResponse;
import com.huertos.dto.VerificarActividadMspRequest;
import java.util.List;

public interface MspRevisionService {

    /**
     * Lista las actividades ancestrales que esperan dictamen del Técnico MSP, con la bitácora,
     * las evidencias y la nómina de participantes de cada una.
     *
     * @return Actividades pendientes de verificación, de la más antigua a la más reciente
     */
    List<ActividadRevisionResponse> listarPendientes();

    /**
     * Emite el dictamen del Técnico MSP sobre una actividad ancestral pendiente: guarda el nuevo estado,
     * la observación, el verificador y la fecha de verificación. Al aprobar, delega la acreditación de
     * puntos a la comunidad, al huerto y a los participantes (GM-CU01, GM-CU03).
     *
     * @param correoTecnico Correo electrónico del Técnico MSP autenticado
     * @param actividadId Identificador de la actividad ancestral
     * @param request Dictamen con el nuevo estado y la observación que lo justifica
     * @return Resultado del dictamen con el estado, los puntos acreditados y la fecha de verificación
     */
    ResultadoVerificacionResponse dictaminarActividad(String correoTecnico, Long actividadId,
            VerificarActividadMspRequest request);
}
