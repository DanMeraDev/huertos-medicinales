package com.huertos.service;

import com.huertos.dto.InsigniaMiembroResponse;
import com.huertos.dto.RegistrarLaborMiembroRequest;
import com.huertos.dto.ResultadoAcreditacionMiembroResponse;
import java.util.List;

/**
 * Servicio para registrar las jornadas y labores desempeñadas por miembros voluntarios
 * y gestionar la acreditación de puntos de gamificación y talentos.
 */
public interface RegistroActividadMiembroService {

    /**
     * Registra una labor desempeñada por un voluntario acreditando puntos exclusivamente al miembro,
     * evaluando su ascenso de nivel y desbloqueo de talentos comunitarios.
     *
     * @param correoResponsable correo del responsable de comunidad autenticado
     * @param request datos de la labor a registrar
     * @return resultado de la acreditación con puntos, nivel y talentos desbloqueados
     */
    ResultadoAcreditacionMiembroResponse registrarLabor(String correoResponsable, RegistrarLaborMiembroRequest request);

    /**
     * Lista las insignias y talentos comunitarios obtenidos por un miembro voluntario.
     *
     * @param correoResponsable correo del responsable de comunidad autenticado
     * @param miembroId identificador del miembro voluntario
     * @return lista de insignias obtenidas por el miembro
     */
    List<InsigniaMiembroResponse> listarInsigniasMiembro(String correoResponsable, Long miembroId);
}
