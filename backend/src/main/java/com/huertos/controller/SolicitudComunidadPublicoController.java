package com.huertos.controller;

import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.SolicitudComunidadResponse;
import com.huertos.service.SolicitudComunidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publico/solicitudes-comunidad")
@RequiredArgsConstructor
@Tag(name = "Solicitudes de comunidad", description = "Postulación pública de nuevas comunidades")
public class SolicitudComunidadPublicoController {

    private final SolicitudComunidadService solicitudComunidadService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una solicitud pública para crear una comunidad")
    public SolicitudComunidadResponse registrarSolicitud(@Valid @RequestBody CrearSolicitudComunidadRequest request) {
        return solicitudComunidadService.registrarSolicitud(request);
    }
}
