package com.huertos.dto;

import java.util.List;

public record PaginaResponse<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {
}
