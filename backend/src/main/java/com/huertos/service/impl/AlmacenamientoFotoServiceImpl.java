package com.huertos.service.impl;

import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.service.AlmacenamientoFotoService;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlmacenamientoFotoServiceImpl implements AlmacenamientoFotoService {

    private static final String RUTA_FOTOS = "/api/plantas/fotos/";

    private final Path directorio;

    public AlmacenamientoFotoServiceImpl(@Value("${app.fotos.directorio}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(MultipartFile foto) {
        String nombreArchivo = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(foto.getOriginalFilename());
        try (InputStream contenido = foto.getInputStream()) {
            Files.createDirectories(directorio);
            Files.copy(contenido, directorio.resolve(nombreArchivo));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return RUTA_FOTOS + nombreArchivo;
    }

    @Override
    public Resource cargar(String nombreArchivo) {
        Path ruta = directorio.resolve(nombreArchivo).normalize();
        if (!directorio.equals(ruta.getParent()) || !Files.isRegularFile(ruta)) {
            throw new RecursoNoEncontradoException("No existe la fotografía solicitada");
        }
        return new FileSystemResource(ruta);
    }
}
