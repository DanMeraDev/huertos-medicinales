package com.huertos.exception;

public class FormatoImagenNoAdmitidoException extends RuntimeException {

    public FormatoImagenNoAdmitidoException() {
        super("Formato no admitido. Seleccione una imagen JPG, PNG o WEBP");
    }
}
