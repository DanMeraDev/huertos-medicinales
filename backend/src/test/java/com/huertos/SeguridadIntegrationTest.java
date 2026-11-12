package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/**
 * Usa un servidor real, porque MockMvc no pasa por el despacho a /error.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class SeguridadIntegrationTest {

    @LocalServerPort
    private int puerto;

    @Test
    void unaRutaPublicaConservaSuEstadoDeErrorSinSesion() throws Exception {
        assertThat(estadoDe("/api/auth/inexistente")).isEqualTo(404);
    }

    @Test
    void unaRutaPrivadaSinSesionDevuelve401() throws Exception {
        assertThat(estadoDe("/api/comunidades")).isEqualTo(401);
    }

    private int estadoDe(String ruta) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta)).build();
        try (HttpClient cliente = HttpClient.newHttpClient()) {
            return cliente.send(peticion, HttpResponse.BodyHandlers.discarding()).statusCode();
        }
    }
}
