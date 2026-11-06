package com.huertos.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guarda y lee las fotografías de las plantas en la carpeta configurada en {@code app.fotos.directorio}.
 */
public interface AlmacenamientoFotoService {

    /**
     * Guarda la foto con un nombre UUID y su extensión original, creando la carpeta si no existe.
     *
     * @param foto archivo subido
     * @return la URL pública de la foto, {@code /api/plantas/fotos/{nombreArchivo}}
     */
    String guardar(MultipartFile foto);

    /**
     * Lee una foto guardada.
     *
     * @param nombreArchivo nombre con el que se guardó la foto
     * @return el contenido de la foto
     * @throws com.huertos.exception.RecursoNoEncontradoException si la foto no existe en la carpeta
     */
    Resource cargar(String nombreArchivo);
}
