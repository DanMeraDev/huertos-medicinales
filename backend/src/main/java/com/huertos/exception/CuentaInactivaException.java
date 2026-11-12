package com.huertos.exception;

public class CuentaInactivaException extends RuntimeException {

    public CuentaInactivaException(String mensaje) {
        super(mensaje);
    }
}
