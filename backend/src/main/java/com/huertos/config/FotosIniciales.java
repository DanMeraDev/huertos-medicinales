package com.huertos.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class FotosIniciales implements ApplicationRunner {

    private final Path directorioPlantas;

    public FotosIniciales(@Value("${huertos.fotos.directorio}") String directorio) {
        this.directorioPlantas = Path.of(directorio).resolve("plantas");
    }

    @Override
    public void run(ApplicationArguments args) {
    }
}
