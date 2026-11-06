package com.huertos.controller;

import com.huertos.dto.CrearPlantaRequest;
import com.huertos.dto.PlantaResponse;
import com.huertos.service.AlmacenamientoFotoService;
import com.huertos.service.PlantaMedicinalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/plantas")
@RequiredArgsConstructor
@Tag(name = "Plantas medicinales", description = "Catálogo de plantas medicinales")
public class PlantaMedicinalController {

    private final PlantaMedicinalService plantaMedicinalService;
    private final AlmacenamientoFotoService almacenamientoFotoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('RESPONSABLE_COMUNIDAD', 'ADMIN_MSP')")
    @Operation(summary = "Registra una planta medicinal con su fotografía")
    public ResponseEntity<PlantaResponse> registrar(
            @RequestPart("datos") @Valid CrearPlantaRequest datos,
            @RequestPart(value = "foto", required = false) MultipartFile foto) {
        PlantaResponse planta = plantaMedicinalService.registrar(datos, foto);
        return ResponseEntity.created(URI.create("/api/plantas/" + planta.id())).body(planta);
    }

    @GetMapping("/fotos/{nombreArchivo}")
    @Operation(summary = "Devuelve la fotografía de una planta")
    public ResponseEntity<Resource> obtenerFoto(@PathVariable String nombreArchivo) {
        Resource foto = almacenamientoFotoService.cargar(nombreArchivo);
        MediaType tipo = MediaTypeFactory.getMediaType(foto).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(tipo).body(foto);
    }
}
