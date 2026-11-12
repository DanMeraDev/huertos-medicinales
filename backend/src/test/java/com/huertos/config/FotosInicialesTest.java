package com.huertos.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;

class FotosInicialesTest {

    @TempDir
    private Path directorio;

    @Test
    void copiaLasFotosInicialesQueFaltan() throws Exception {
        new FotosIniciales(directorio.toString()).run(null);

        assertThat(Files.readAllBytes(directorio.resolve("plantas/manzanilla.jpg")))
                .isEqualTo(deClasspath("fotos-iniciales/plantas/manzanilla.jpg"));
        assertThat(directorio.resolve("plantas/cola-de-caballo.jpg")).isRegularFile();
        assertThat(directorio.resolve("plantas/creditos.csv")).doesNotExist();
    }

    @Test
    void noSobrescribeUnaFotoQueYaExiste() throws Exception {
        byte[] propia = "foto cambiada por el tecnico".getBytes(StandardCharsets.UTF_8);
        Files.createDirectories(directorio.resolve("plantas"));
        Files.write(directorio.resolve("plantas/romero.jpg"), propia);

        new FotosIniciales(directorio.toString()).run(null);

        assertThat(Files.readAllBytes(directorio.resolve("plantas/romero.jpg"))).isEqualTo(propia);
        assertThat(directorio.resolve("plantas/ruda.jpg")).isRegularFile();
    }

    private static byte[] deClasspath(String ruta) throws IOException {
        try (InputStream entrada = new ClassPathResource(ruta).getInputStream()) {
            return entrada.readAllBytes();
        }
    }
}
