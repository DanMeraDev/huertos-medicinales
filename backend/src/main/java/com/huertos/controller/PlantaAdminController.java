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
@RequestMapping("/api/msp/plantas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TECNICO_MSP')")
@Tag(name = "Administración del catálogo de plantas", description = "Gestión del catálogo por el técnico MSP")
public class PlantaAdminController {

    private final AlmacenamientoService almacenamientoService;

    @PostMapping(value = "/fotografia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Sube la fotografía de una planta (JPG, PNG o WEBP hasta 5 MB) y devuelve su ruta")
    public FotografiaSubidaResponse subirFotografia(@RequestParam("archivo") MultipartFile archivo) {
        return new FotografiaSubidaResponse(almacenamientoService.guardar(archivo, "plantas"));
    }
}
