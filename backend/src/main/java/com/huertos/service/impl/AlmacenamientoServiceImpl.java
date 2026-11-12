package com.huertos.service.impl;

import com.huertos.service.AlmacenamientoService;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    private final Path directorioBase;

    public AlmacenamientoServiceImpl(@Value("${huertos.fotos.directorio}") String directorio) {
        this.directorioBase = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(MultipartFile archivo, String subcarpeta) {
        return null;
    }

    @Override
    public Optional<Path> obtener(String subcarpeta, String archivo) {
        return Optional.empty();
    }
}
