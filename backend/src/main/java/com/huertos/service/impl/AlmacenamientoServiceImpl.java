package com.huertos.service.impl;

import com.huertos.exception.FormatoImagenNoAdmitidoException;
import com.huertos.exception.ImagenDemasiadoGrandeException;
import com.huertos.service.AlmacenamientoService;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    private static final long TAMANIO_MAXIMO = 5L * 1024 * 1024;
    private static final Set<String> SUBCARPETAS_PERMITIDAS = Set.of("plantas", "actividades");
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[a-z0-9-]+\\.(jpg|png|webp)$");
    private static final byte[] FIRMA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] FIRMA_RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] FIRMA_WEBP = "WEBP".getBytes(StandardCharsets.US_ASCII);
    private static final int POSICION_FIRMA_WEBP = 8;
    private static final int LONGITUD_CABECERA = 12;

    private final Path directorioBase;

    public AlmacenamientoServiceImpl(@Value("${app.fotos.directorio}") String directorio) {
        this.directorioBase = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(MultipartFile archivo, String subcarpeta) {
        if (!SUBCARPETAS_PERMITIDAS.contains(subcarpeta)) {
            throw new IllegalArgumentException("Subcarpeta no permitida: " + subcarpeta);
        }
        if (archivo.getSize() > TAMANIO_MAXIMO) {
            throw new ImagenDemasiadoGrandeException();
        }
        String ruta = subcarpeta + "/" + UUID.randomUUID() + "." + extensionSegunFirma(archivo);
        Path destino = directorioBase.resolve(ruta);
        try (InputStream contenido = archivo.getInputStream()) {
            Files.createDirectories(destino.getParent());
            Files.copy(contenido, destino);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ruta;
    }

    @Override
    public Optional<Path> obtener(String subcarpeta, String archivo) {
        if (!SUBCARPETAS_PERMITIDAS.contains(subcarpeta) || !NOMBRE_VALIDO.matcher(archivo).matches()) {
            return Optional.empty();
        }
        Path ruta = directorioBase.resolve(subcarpeta).resolve(archivo).normalize();
        if (!ruta.startsWith(directorioBase) || !Files.isRegularFile(ruta)) {
            return Optional.empty();
        }
        return Optional.of(ruta);
    }

    private String extensionSegunFirma(MultipartFile archivo) {
        byte[] cabecera = leerCabecera(archivo);
        if (empiezaCon(cabecera, FIRMA_JPEG, 0)) {
            return "jpg";
        }
        if (empiezaCon(cabecera, FIRMA_PNG, 0)) {
            return "png";
        }
        if (empiezaCon(cabecera, FIRMA_RIFF, 0) && empiezaCon(cabecera, FIRMA_WEBP, POSICION_FIRMA_WEBP)) {
            return "webp";
        }
        throw new FormatoImagenNoAdmitidoException();
    }

    private static byte[] leerCabecera(MultipartFile archivo) {
        try (InputStream contenido = archivo.getInputStream()) {
            return contenido.readNBytes(LONGITUD_CABECERA);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean empiezaCon(byte[] datos, byte[] firma, int desde) {
        int hasta = desde + firma.length;
        return datos.length >= hasta && Arrays.equals(datos, desde, hasta, firma, 0, firma.length);
    }
}
