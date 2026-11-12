package com.huertos.service;

import com.huertos.dto.RankingComunidadResponse;
import java.util.List;

public interface RankingService {

    /**
     * Obtiene el ranking general de comunidades activas ordenadas por puntos de mayor a menor.
     *
     * @return lista de comunidades con su respectiva posicion ordinal en el ranking
     */
    List<RankingComunidadResponse> obtenerRankingComunidades();
}
