package com.huertos.service;

import java.nio.file.Path;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public interface AlmacenamientoService {

    /**
     * Guarda una imagen en el directorio de fotografías con un nombre UUID. El formato se reconoce por la firma
     * de bytes del contenido, no por la extensión ni por el Content-Type enviados, y la extensión del archivo
     * guardado es la del formato reconocido.
     *
     * @param archivo imagen JPEG, PNG o WEBP de hasta 5 MB
     * @param subcarpeta carpeta de destino: {@code plantas} o {@code actividades}
     * @return la ruta relativa del archivo guardado, por ejemplo {@code plantas/<uuid>.jpg}
     * @throws IllegalArgumentException si la subcarpeta no está permitida
     * @throws com.huertos.exception.ImagenDemasiadoGrandeException si la imagen pesa más de 5 MB
     * @throws com.huertos.exception.FormatoImagenNoAdmitidoException si el archivo está vacío o su contenido
     *         no es JPEG, PNG ni WEBP
     */
    String guardar(MultipartFile archivo, String subcarpeta);

    /**
     * Busca una fotografía guardada. Solo acepta subcarpetas permitidas y nombres en minúsculas con letras,
     * dígitos y guiones terminados en .jpg, .png o .webp, y nunca devuelve una ruta fuera del directorio
     * de fotografías.
     *
     * @param subcarpeta carpeta donde se busca: {@code plantas} o {@code actividades}
     * @param archivo nombre del archivo, por ejemplo {@code manzanilla.jpg}
     * @return la ruta absoluta del archivo, o vacío si no existe o la subcarpeta o el nombre no son válidos
     */
    Optional<Path> obtener(String subcarpeta, String archivo);
}
