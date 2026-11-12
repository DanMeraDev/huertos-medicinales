package com.huertos.controller;

import com.huertos.dto.InsigniaMiembroResponse;
import com.huertos.dto.RegistrarLaborMiembroRequest;
import com.huertos.dto.ResultadoAcreditacionMiembroResponse;
import com.huertos.service.RegistroActividadMiembroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comunidad/actividades-miembros")
@RequiredArgsConstructor
@Tag(name = "Actividades de Miembros", description = "Registro de jornadas cotidianas y acreditación de puntos a miembros")
public class RegistroActividadMiembroController {

    private final RegistroActividadMiembroService registroActividadMiembroService;

    @PostMapping
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    @Operation(summary = "Registra una labor cotidiana a un miembro voluntario y acredita sus puntos")
    public ResponseEntity<ResultadoAcreditacionMiembroResponse> registrarLabor(
            Authentication authentication,
            @Valid @RequestBody RegistrarLaborMiembroRequest request) {
        ResultadoAcreditacionMiembroResponse respuesta = registroActividadMiembroService
                .registrarLabor(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping("/miembros/{miembroId}/insignias")
    @PreAuthorize("hasRole('RESPONSABLE_COMUNIDAD')")
    @Operation(summary = "Lista las insignias y talentos comunitarios obtenidos por un voluntario")
    public ResponseEntity<List<InsigniaMiembroResponse>> listarInsigniasMiembro(
            @PathVariable Long miembroId,
            Authentication authentication) {
        List<InsigniaMiembroResponse> insignias = registroActividadMiembroService
                .listarInsigniasMiembro(authentication.getName(), miembroId);
        return ResponseEntity.ok(insignias);
    }
}
