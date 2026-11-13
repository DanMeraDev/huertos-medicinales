package com.huertos.service;

import com.huertos.dto.RankingComunidadResponse;
import com.huertos.dto.RankingMiembroResponse;
import java.util.List;

public interface RankingService {

    /**
     * Obtiene el ranking general de comunidades activas ordenadas por puntos de mayor a menor.
     *
     * @return lista de comunidades con su respectiva posicion ordinal en el ranking
     */
    List<RankingComunidadResponse> obtenerRankingComunidades();

    /**
     * Obtiene el ranking general de miembros/voluntarios activos ordenados por puntos de mayor a menor.
     *
     * @return lista de miembros con su respectiva posicion ordinal y total de insignias
     */
    List<RankingMiembroResponse> obtenerRankingMiembros();
}
