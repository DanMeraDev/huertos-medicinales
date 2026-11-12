package com.huertos.service;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaDetalleResponse;

public interface PlantaAdminService {

    /**
     * Registra una planta nueva en el catálogo como activa, con la fecha de creación y de actualización del reloj
     * del sistema. Se recortan los espacios del inicio y del final de los textos, y los textos opcionales vacíos se
     * guardan como nulos. El nombre científico es único sin distinguir mayúsculas, incluidas las plantas dadas de
     * baja.
     *
     * @param request datos de la planta
     * @return la ficha técnica de la planta registrada
     * @throws com.huertos.exception.ReglaNegocioException si la altitud mínima es mayor que la máxima
     * @throws com.huertos.exception.ConflictoException si el nombre científico ya existe en el catálogo
     */
    PlantaDetalleResponse registrarPlanta(CrearPlantaRequest request);
}
