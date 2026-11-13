package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.huertos.exception.FormatoImagenNoAdmitidoException;
import com.huertos.exception.ImagenDemasiadoGrandeException;
import com.huertos.service.impl.AlmacenamientoServiceImpl;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class AlmacenamientoServiceImplTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46};
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
    private static final byte[] WEBP = "RIFF$\u0000\u0000\u0000WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1);
    private static final int CINCO_MB = 5 * 1024 * 1024;

    @TempDir
    private Path directorio;

    private AlmacenamientoServiceImpl servicio() {
        return new AlmacenamientoServiceImpl(directorio.toString());
    }

    @Test
    void guardaUnJpegConNombreUuidYDevuelveLaRutaRelativa() throws IOException {
        String ruta = servicio().guardar(archivo("foto.jpg", JPEG), "plantas");

        assertThat(ruta).matches("plantas/[0-9a-f-]{36}\\.jpg");
        assertThat(UUID.fromString(ruta.substring("plantas/".length(), ruta.length() - ".jpg".length()))).isNotNull();
        assertThat(Files.readAllBytes(directorio.resolve(ruta))).isEqualTo(JPEG);
    }

    @Test
    void guardaUnPngConLaExtensionSegunLaFirma() throws IOException {
        String ruta = servicio().guardar(archivo("foto.jpg", PNG), "plantas");

        assertThat(ruta).matches("plantas/[0-9a-f-]{36}\\.png");
        assertThat(Files.readAllBytes(directorio.resolve(ruta))).isEqualTo(PNG);
    }

    @Test
    void guardaUnWebpEnLaSubcarpetaDeActividades() throws IOException {
        String ruta = servicio().guardar(archivo("foto.png", WEBP), "actividades");

        assertThat(ruta).matches("actividades/[0-9a-f-]{36}\\.webp");
        assertThat(Files.readAllBytes(directorio.resolve(ruta))).isEqualTo(WEBP);
    }

    @Test
    void rechazaUnJpgQueEnRealidadEsTexto() {
        MockMultipartFile texto = archivo("foto.jpg", "no soy una imagen".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> servicio().guardar(texto, "plantas"))
                .isInstanceOf(FormatoImagenNoAdmitidoException.class)
                .hasMessage("Formato no admitido. Seleccione una imagen JPG, PNG o WEBP");
    }

    @Test
    void rechazaUnArchivoVacio() {
        assertThatThrownBy(() -> servicio().guardar(archivo("foto.jpg", new byte[0]), "plantas"))
                .isInstanceOf(FormatoImagenNoAdmitidoException.class);
    }

    @Test
    void rechazaUnaImagenDeMasDeCincoMegas() {
        byte[] grande = Arrays.copyOf(JPEG, CINCO_MB + 1);

        assertThatThrownBy(() -> servicio().guardar(archivo("foto.jpg", grande), "plantas"))
                .isInstanceOf(ImagenDemasiadoGrandeException.class)
                .hasMessage("La imagen supera el límite de 5 MB. Intente reducir la resolución");
    }

    @Test
    void aceptaUnaImagenDeExactamenteCincoMegas() {
        byte[] limite = Arrays.copyOf(JPEG, CINCO_MB);

        assertThat(servicio().guardar(archivo("foto.jpg", limite), "plantas")).endsWith(".jpg");
    }

    @Test
    void rechazaSubcarpetasNoPermitidas() {
        assertThatThrownBy(() -> servicio().guardar(archivo("foto.jpg", JPEG), "usuarios"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio().guardar(archivo("foto.jpg", JPEG), "../x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtieneUnaFotografiaGuardada() {
        AlmacenamientoServiceImpl servicio = servicio();
        String ruta = servicio.guardar(archivo("foto.jpg", JPEG), "plantas");
        String nombre = ruta.substring("plantas/".length());

        assertThat(servicio.obtener("plantas", nombre)).contains(directorio.resolve(ruta).toAbsolutePath().normalize());
    }

    @Test
    void noObtieneUnaFotografiaInexistente() {
        assertThat(servicio().obtener("plantas", "manzanilla.jpg")).isEmpty();
    }

    @Test
    void rechazaRutasQueSalenDelDirectorio() throws IOException {
        Files.write(directorio.resolve("secreto.jpg"), JPEG);
        Files.createDirectories(directorio.resolve("plantas"));

        assertThat(servicio().obtener("plantas", "../secreto.jpg")).isEmpty();
        assertThat(servicio().obtener("..", "secreto.jpg")).isEmpty();
    }

    @Test
    void rechazaNombresYSubcarpetasNoValidos() throws IOException {
        Files.createDirectories(directorio.resolve("otros"));
        Files.write(directorio.resolve("otros/manzanilla.jpg"), JPEG);
        Files.createDirectories(directorio.resolve("plantas"));
        Files.write(directorio.resolve("plantas/Manzanilla.JPG"), JPEG);
        Files.write(directorio.resolve("plantas/manzanilla.gif"), JPEG);

        assertThat(servicio().obtener("otros", "manzanilla.jpg")).isEmpty();
        assertThat(servicio().obtener("plantas", "Manzanilla.JPG")).isEmpty();
        assertThat(servicio().obtener("plantas", "manzanilla.gif")).isEmpty();
    }

    private static MockMultipartFile archivo(String nombre, byte[] contenido) {
        return new MockMultipartFile("archivo", nombre, "image/jpeg", contenido);
    }
}
