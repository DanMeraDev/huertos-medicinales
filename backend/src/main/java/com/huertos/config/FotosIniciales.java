package com.huertos.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Copia al arrancar las fotografías de las plantas del MSP que trae el jar, para que las rutas de la
 * migración V8 tengan archivo. Nunca sobrescribe una fotografía que ya esté en el directorio.
 */
@Component
public class FotosIniciales implements ApplicationRunner {

    private static final String PATRON_FOTOS = "classpath:fotos-iniciales/plantas/*";

    private final Path directorioPlantas;

    public FotosIniciales(@Value("${app.fotos.directorio}") String directorio) {
        this.directorioPlantas = Path.of(directorio).resolve("plantas");
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Files.createDirectories(directorioPlantas);
        for (Resource foto : new PathMatchingResourcePatternResolver().getResources(PATRON_FOTOS)) {
            if (foto.isReadable() && foto.getFilename() != null) {
                copiarSiFalta(foto, directorioPlantas.resolve(foto.getFilename()));
            }
        }
    }

    private static void copiarSiFalta(Resource foto, Path destino) throws IOException {
        if (Files.notExists(destino)) {
            try (InputStream contenido = foto.getInputStream()) {
                Files.copy(contenido, destino);
            }
        }
    }
}
