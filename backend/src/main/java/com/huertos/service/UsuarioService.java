package com.huertos.service;

import com.huertos.dto.CambiarClaveRequest;
import com.huertos.dto.CambioClaveResponse;
import com.huertos.dto.CrearUsuarioRequest;
import com.huertos.dto.UsuarioDetalleResponse;
import com.huertos.model.Usuario;

public interface UsuarioService {

    /**
     * Crea una cuenta activa que debe cambiar su contraseña en el primer acceso.
     *
     * @throws com.huertos.exception.ReglaNegocioException si la persona es menor de 18 años
     * @throws com.huertos.exception.ConflictoException si la cédula o el correo ya están registrados
     */
    UsuarioDetalleResponse crearUsuario(CrearUsuarioRequest request);

    /**
     * Aplica las mismas reglas de {@link #crearUsuario} y devuelve la entidad guardada,
     * para que otros servicios (por ejemplo, la aprobación de una solicitud) creen la cuenta del responsable.
     */
    Usuario crearUsuarioInterno(CrearUsuarioRequest request);

    /**
     * Reemplaza la contraseña del usuario identificado por su correo y quita la obligación de cambiarla.
     *
     * @throws com.huertos.exception.ReglaNegocioException si la confirmación no coincide o la contraseña actual es incorrecta
     * @throws com.huertos.exception.ConflictoException si la nueva contraseña es igual a la actual
     */
    CambioClaveResponse cambiarClave(String correo, CambiarClaveRequest request);
}
