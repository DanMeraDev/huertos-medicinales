package com.huertos.service;

import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaResumenResponse;

public interface PlantaService {

    /**
     * Lista las plantas activas del catálogo público ordenadas por nombre común ascendente.
     * Una página negativa se consulta como la página 0 y el tamaño se limita a 100 plantas.
     *
     * @param pagina número de página, empezando en 0
     * @param tamanio cantidad de plantas por página
     * @return la página de plantas en formato resumido
     */
    PaginaResponse<PlantaResumenResponse> listarPlantasPublicas(int pagina, int tamanio);
}
