package com.huertos.controller;

import com.huertos.dto.CrearUsuarioRequest;
import com.huertos.dto.UsuarioDetalleResponse;
import com.huertos.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de cuentas de usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TECNICO_SIS')")
    @Operation(summary = "Crea una cuenta de usuario con contraseña temporal")
    public UsuarioDetalleResponse crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        return usuarioService.crearUsuario(request);
    }
}
