package com.huertos.controller;

import com.huertos.dto.ActualizarRolRequest;
import com.huertos.dto.UsuarioResponse;
import com.huertos.service.AdminUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN_MSP')")
@RequiredArgsConstructor
@Tag(name = "Administración de usuarios", description = "Consulta de usuarios y cambio de roles")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    @GetMapping
    @Operation(summary = "Lista los usuarios registrados")
    public List<UsuarioResponse> listarUsuarios() {
        return adminUsuarioService.listarUsuarios();
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Actualiza el rol de un usuario")
    public UsuarioResponse actualizarRol(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarRolRequest request,
            Authentication autenticacion) {
        return adminUsuarioService.actualizarRol(id, request, autenticacion.getName());
    }
}
