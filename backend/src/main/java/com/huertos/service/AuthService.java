package com.huertos.service;

import com.huertos.dto.LoginRequest;
import com.huertos.dto.LoginResponse;

public interface AuthService {

    /**
     * Valida el correo y la contraseña y entrega un token JWT con el id, el rol y si debe cambiar la clave.
     * La contraseña se comprueba antes que el estado de la cuenta, para no revelar si una cuenta existe.
     *
     * @throws com.huertos.exception.CredencialesInvalidasException si el correo no existe o la contraseña no coincide
     * @throws com.huertos.exception.CuentaInactivaException si la cuenta está desactivada
     */
    LoginResponse iniciarSesion(LoginRequest request);
}
