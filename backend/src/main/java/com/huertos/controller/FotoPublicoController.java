package com.huertos.controller;

import com.huertos.exception.RecursoNoEncontradoException;
import com.huertos.service.AlmacenamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico/fotos")
@RequiredArgsConstructor
@Tag(name = "Fotografías públicas")
public class FotoPublicoController {

    private static final Map<String, MediaType> TIPOS_POR_EXTENSION = Map.of(
            "jpg", MediaType.IMAGE_JPEG,
            "png", MediaType.IMAGE_PNG,
            "webp", MediaType.parseMediaType("image/webp"));

    private final AlmacenamientoService almacenamientoService;

    @GetMapping("/{subcarpeta}/{archivo}")
    @Operation(summary = "Entrega una fotografía guardada, por ejemplo plantas/manzanilla.jpg")
    public ResponseEntity<Resource> obtenerFotografia(@PathVariable String subcarpeta, @PathVariable String archivo) {
        return almacenamientoService.obtener(subcarpeta, archivo)
                .map(ruta -> ResponseEntity.ok()
                        .contentType(TIPOS_POR_EXTENSION.get(archivo.substring(archivo.lastIndexOf('.') + 1)))
                        .header("X-Content-Type-Options", "nosniff")
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                        .<Resource>body(new FileSystemResource(ruta)))
                .orElseThrow(() -> new RecursoNoEncontradoException("La fotografía solicitada no existe"));
    }
}
