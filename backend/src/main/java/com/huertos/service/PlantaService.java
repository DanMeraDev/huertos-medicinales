package com.huertos.service;

import com.huertos.dto.FiltrosPlantaRequest;
import com.huertos.dto.PaginaResponse;
import com.huertos.dto.PlantaDetalleResponse;
import com.huertos.dto.PlantaResumenResponse;
import java.util.List;

public interface PlantaService {

    /**
     * Lista las plantas activas del catálogo público ordenadas por nombre común ascendente y, a igual nombre, por id.
     * Una página negativa se consulta como la página 0 y el tamaño se limita a 100 plantas.
     *
     * @param pagina número de página, empezando en 0
     * @param tamanio cantidad de plantas por página
     * @return la página de plantas en formato resumido
     */
    PaginaResponse<PlantaResumenResponse> listarPlantasPublicas(int pagina, int tamanio);

    /**
     * Busca las plantas activas cuyo nombre común, científico o kichwa contiene el criterio, sin distinguir
     * mayúsculas ni tildes, ordenadas por nombre común y, a igual nombre, por id. Los caracteres % y _ del
     * criterio se buscan como texto literal.
     *
     * @param criterio texto a buscar; se recortan los espacios del inicio y del final
     * @return las plantas encontradas en formato resumido
     * @throws com.huertos.exception.ReglaNegocioException si el criterio es nulo o, sin espacios al inicio
     *         ni al final, tiene menos de 2 o más de 100 caracteres
     */
    List<PlantaResumenResponse> buscarPorNombre(String criterio);

    /**
     * Filtra las plantas activas del catálogo combinando los filtros recibidos, ordenadas por nombre común y,
     * a igual nombre, por id. Los filtros nulos se ignoran, igual que una región vacía o en blanco; sin ningún
     * filtro devuelve todas las plantas activas. Una planta sin rango de altitud registrado no se excluye al
     * filtrar por altitud. La región y el criterio se buscan sin distinguir mayúsculas ni tildes y los
     * caracteres % y _ se toman como texto literal.
     *
     * @param filtros filtros opcionales y combinables
     * @return las plantas que cumplen todos los filtros en formato resumido
     * @throws com.huertos.exception.ReglaNegocioException si la altitud está fuera de 0 a 6000 msnm o si el
     *         criterio, sin espacios al inicio ni al final, tiene menos de 2 o más de 100 caracteres
     */
    List<PlantaResumenResponse> filtrarPlantas(FiltrosPlantaRequest filtros);

    /**
     * Obtiene la ficha técnica completa de una planta activa del catálogo público.
     *
     * @param id identificador de la planta
     * @return la ficha técnica de la planta
     * @throws com.huertos.exception.RecursoNoEncontradoException si la planta no existe o fue dada de baja
     */
    PlantaDetalleResponse obtenerDetalle(Long id);
}
