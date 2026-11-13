package com.huertos.controller;

import com.huertos.dto.FotografiaSubidaResponse;
import com.huertos.service.AlmacenamientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/actividades-ancestrales/fotografia")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
@Tag(name = "Evidencias de actividades ancestrales", description = "Subida de fotografías de actividades para revisión del MSP")
public class ActividadFotoController {

    private final AlmacenamientoService almacenamientoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Sube la fotografía de evidencia de una actividad ancestral (JPG, PNG o WEBP hasta 5 MB) y devuelve su ruta relativa")
    public FotografiaSubidaResponse subirFotografia(@RequestParam("archivo") MultipartFile archivo) {
        return new FotografiaSubidaResponse(almacenamientoService.guardar(archivo, "actividades"));
    }
}
