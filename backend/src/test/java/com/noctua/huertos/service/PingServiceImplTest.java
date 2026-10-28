package com.noctua.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.noctua.huertos.dto.PingResponse;
import com.noctua.huertos.repository.PingRepository;
import com.noctua.huertos.service.impl.PingServiceImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PingServiceImplTest {

    private static final Instant AHORA = Instant.parse("2026-10-31T12:00:00Z");

    @Mock
    private PingRepository pingRepository;

    @Test
    void devuelveEstadoOkConLaVersionDeLaBase() {
        when(pingRepository.versionBaseDeDatos()).thenReturn("11.4.8-MariaDB");
        PingService servicio = new PingServiceImpl(pingRepository, Clock.fixed(AHORA, ZoneOffset.UTC));

        PingResponse respuesta = servicio.ping();

        assertThat(respuesta.estado()).isEqualTo("ok");
        assertThat(respuesta.baseDeDatos()).isEqualTo("11.4.8-MariaDB");
        assertThat(respuesta.fecha()).isEqualTo(AHORA);
    }
}
