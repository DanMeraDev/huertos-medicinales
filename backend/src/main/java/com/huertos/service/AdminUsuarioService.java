package com.huertos.service;

import com.huertos.dto.ActualizarRolRequest;
import com.huertos.dto.UsuarioResponse;
import java.util.List;

/**
 * Gestión de usuarios y roles por parte del administrador del MSP.
 */
public interface AdminUsuarioService {

    /**
     * Lista todos los usuarios registrados.
     *
     * @return los usuarios del sistema
     */
    List<UsuarioResponse> listarUsuarios();

    /**
     * Cambia el rol de un usuario y deja la traza en la auditoría. El nuevo rol aplica
     * desde el próximo inicio de sesión del usuario, porque el rol viaja en el token.
     * No se permite quitar el rol al único administrador activo.
     *
     * @param usuarioId          usuario al que se le cambia el rol
     * @param request            nuevo rol
     * @param emailAdministrador correo del administrador que hace el cambio
     * @return el usuario actualizado
     */
    UsuarioResponse actualizarRol(Long usuarioId, ActualizarRolRequest request, String emailAdministrador);
}
