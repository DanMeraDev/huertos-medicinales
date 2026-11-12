package com.huertos.controller;

import com.huertos.dto.RechazarSolicitudRequest;
import com.huertos.dto.ResolucionSolicitudResponse;
import com.huertos.dto.SolicitudDetalleCompletoResponse;
import com.huertos.dto.SolicitudResumenResponse;
import com.huertos.model.EstadoSolicitud;
import com.huertos.service.SolicitudComunidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitudes-comunidad")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TECNICO_MSP')")
@Tag(name = "Revisión de solicitudes", description = "Aprobación o rechazo de solicitudes de comunidad por el técnico MSP")
public class SolicitudComunidadController {

    private final SolicitudComunidadService solicitudComunidadService;

    @GetMapping
    @Operation(summary = "Lista las solicitudes de comunidad en el estado indicado")
    public List<SolicitudResumenResponse> listarPorEstado(@RequestParam EstadoSolicitud estado) {
        return solicitudComunidadService.listarPorEstado(estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene la ficha completa de una solicitud")
    public SolicitudDetalleCompletoResponse obtenerPorId(@PathVariable Long id) {
        return solicitudComunidadService.obtenerPorId(id);
    }

    @PostMapping("/{id}/aprobar")
    @Operation(summary = "Aprueba la solicitud y crea la comunidad y la cuenta del responsable")
    public ResolucionSolicitudResponse aprobar(@PathVariable Long id, Authentication authentication) {
        return solicitudComunidadService.aprobarSolicitud(id, authentication.getName());
    }

    @PostMapping("/{id}/rechazar")
    @Operation(summary = "Rechaza la solicitud con el motivo indicado")
    public ResolucionSolicitudResponse rechazar(@PathVariable Long id,
            @Valid @RequestBody RechazarSolicitudRequest request, Authentication authentication) {
        return solicitudComunidadService.rechazarSolicitud(id, request, authentication.getName());
    }
}
