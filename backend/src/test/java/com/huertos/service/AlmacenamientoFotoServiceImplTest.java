package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.service.impl.AlmacenamientoFotoServiceImpl;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

class AlmacenamientoFotoServiceImplTest {

    private static final String PREFIJO_URL = "/api/plantas/fotos/";
    private static final String PATRON_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @TempDir
    private Path carpeta;

    @Test
    void guardaLaFotoConNombreUuidYLaExtensionOriginal() throws Exception {
        byte[] contenido = {10, 20, 30};
        AlmacenamientoFotoService servicio = new AlmacenamientoFotoServiceImpl(carpeta.toString());

        String fotoUrl = servicio.guardar(new MockMultipartFile("foto", "manzanilla.png", "image/png", contenido));

        assertThat(fotoUrl).startsWith(PREFIJO_URL);
        String nombreArchivo = fotoUrl.substring(PREFIJO_URL.length());
        assertThat(nombreArchivo).matches(PATRON_UUID + "\\.png");
        assertThat(Files.readAllBytes(carpeta.resolve(nombreArchivo))).isEqualTo(contenido);
    }

    @Test
    void leeLaFotoGuardadaPorSuNombre() throws Exception {
        byte[] contenido = {1, 2, 3, 4};
        AlmacenamientoFotoService servicio = new AlmacenamientoFotoServiceImpl(carpeta.toString());
        String fotoUrl = servicio.guardar(new MockMultipartFile("foto", "manzanilla.jpg", "image/jpeg", contenido));
        String nombreArchivo = fotoUrl.substring(PREFIJO_URL.length());

        Resource foto = servicio.cargar(nombreArchivo);

        assertThat(nombreArchivo).matches(PATRON_UUID + "\\.jpg");
        assertThat(foto.getContentAsByteArray()).isEqualTo(contenido);
    }
}
