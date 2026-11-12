package com.huertos.controller;

import com.huertos.dto.ActualizarMiembroRequest;
import com.huertos.dto.CambiarEstadoMiembroRequest;
import com.huertos.dto.FichaInternaMiembroResponse;
import com.huertos.dto.MiembroDetalleResponse;
import com.huertos.dto.MiembroEstadoResponse;
import com.huertos.dto.MiembroResumenResponse;
import com.huertos.dto.RegistrarMiembroRequest;
import com.huertos.service.MiembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Miembros de Comunidad", description = "Gestión de miembros y voluntarios de las comunidades")
@RestController
@RequestMapping("/api/comunidades/mi-comunidad/miembros")
@RequiredArgsConstructor
public class ComunidadMiembroController {

    private final MiembroService miembroService;

    @Operation(summary = "Lista los miembros vinculados a la comunidad del responsable autenticado")
    @GetMapping
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public List<MiembroDetalleResponse> listarMiembros(Authentication authentication) {
        return miembroService.listarMiembrosPorCorreo(authentication.getName());
    }

    @Operation(summary = "Lista los miembros activos de la comunidad del responsable")
    @GetMapping("/activos")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public List<MiembroResumenResponse> listarMiembrosActivos(Authentication authentication) {
        return miembroService.listarMiembrosActivosPorCorreo(authentication.getName());
    }

    @Operation(summary = "Registra un nuevo miembro voluntario en la comunidad o vincula uno existente")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public MiembroDetalleResponse registrarMiembro(
            @Valid @RequestBody RegistrarMiembroRequest request,
            Authentication authentication) {
        return miembroService.registrarMiembroPorCorreo(authentication.getName(), request);
    }

    @Operation(summary = "Actualiza el estado de participación (alta o baja) de un miembro de la comunidad")
    @PatchMapping("/{miembroId}/estado")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public MiembroEstadoResponse cambiarEstado(
            @PathVariable Long miembroId,
            @Valid @RequestBody CambiarEstadoMiembroRequest request,
            Authentication authentication) {
        return miembroService.cambiarEstadoPorCorreo(authentication.getName(), miembroId, request);
    }

    @Operation(summary = "Actualiza los datos personales y de contacto de un miembro de la comunidad")
    @PutMapping("/{miembroId}")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public MiembroDetalleResponse actualizarDatosContacto(
            @PathVariable Long miembroId,
            @Valid @RequestBody ActualizarMiembroRequest request,
            Authentication authentication) {
        return miembroService.actualizarDatosContactoPorCorreo(authentication.getName(), miembroId, request);
    }

    @Operation(summary = "Obtiene la ficha interna de un miembro de la comunidad")
    @GetMapping("/{miembroId}")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    public FichaInternaMiembroResponse obtenerFichaInterna(
            @PathVariable Long miembroId,
            Authentication authentication) {
        return miembroService.obtenerFichaInternaPorCorreo(authentication.getName(), miembroId);
    }
}
