package com.huertos.exception;

public class ImagenDemasiadoGrandeException extends RuntimeException {

    public static final String MENSAJE = "La imagen supera el límite de 5 MB. Intente reducir la resolución";

    public ImagenDemasiadoGrandeException() {
        super(MENSAJE);
    }
}
