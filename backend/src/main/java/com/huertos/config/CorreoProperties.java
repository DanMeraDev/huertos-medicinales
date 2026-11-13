package com.huertos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.correo")
public record CorreoProperties(String remitente, String urlFrontend) {
}
