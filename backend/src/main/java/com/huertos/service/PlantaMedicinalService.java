package com.huertos.service;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Catálogo de plantas medicinales.
 */
public interface PlantaMedicinalService {

    String MENSAJE_FOTO_INVALIDA = "Adjunta una fotografía JPG o PNG de máximo 2 MB";

    /**
     * Registra una planta en el catálogo con su fotografía y la fecha de registro del reloj del sistema.
     *
     * @param datos datos de la señalética y del etiquetado MSP
     * @param foto fotografía JPG o PNG de máximo 2 MB
     * @return la planta registrada con la URL de su fotografía
     * @throws com.huertos.exception.ConflictoException si el nombre científico ya está registrado,
     *         sin distinguir mayúsculas ni espacios al inicio o al final
     * @throws com.huertos.exception.ReglaNegocioException si la foto falta, está vacía,
     *         no es JPG o PNG, o pesa más de 2 MB
     */
    PlantaResponse registrar(CrearPlantaRequest datos, MultipartFile foto);
}
