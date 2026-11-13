package com.huertos.service;

import com.huertos.dto.ActualizarPlantaRequest;
import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.DarBajaPlantaRequest;
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

    /**
     * Actualiza la ficha técnica de una planta activa del catálogo y reemplaza sus categorías. Aplica la misma
     * normalización que el registro y guarda al técnico que hace el cambio y la fecha del reloj del sistema. No
     * cambia el estado activo, los datos de baja ni la fecha de creación. El nombre científico no puede pertenecer
     * a otra planta sin distinguir mayúsculas, incluidas las dadas de baja; cambiar solo las mayúsculas del nombre
     * de la misma planta está permitido.
     *
     * @param id identificador de la planta
     * @param request datos nuevos de la planta
     * @param correo correo del técnico autenticado
     * @return la ficha técnica actualizada
     * @throws com.huertos.exception.ReglaNegocioException si la altitud mínima es mayor que la máxima
     * @throws com.huertos.exception.RecursoNoEncontradoException si la planta no existe o fue dada de baja
     * @throws com.huertos.exception.ConflictoException si el nombre científico pertenece a otra planta
     */
    PlantaDetalleResponse actualizarPlanta(Long id, ActualizarPlantaRequest request, String correo);

    /**
     * Da de baja una planta del catálogo con un borrado lógico: la marca como inactiva y guarda el motivo sin los
     * espacios del inicio y del final, la fecha del reloj del sistema y el técnico que hace la baja. La fila no se
     * borra y los demás datos de la planta no cambian, así que las siembras, el material de bodega y la fotografía
     * se conservan. La planta deja de aparecer en el catálogo público.
     *
     * @param id identificador de la planta
     * @param request motivo de la baja
     * @param correo correo del técnico autenticado
     * @throws com.huertos.exception.RecursoNoEncontradoException si la planta no existe
     * @throws com.huertos.exception.ReglaNegocioException si la planta ya está dada de baja
     */
    void darDeBajaPlanta(Long id, DarBajaPlantaRequest request, String correo);
}
