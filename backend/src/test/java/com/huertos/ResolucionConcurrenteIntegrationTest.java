package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;

import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.exception.ConflictoException;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.SolicitudComunidadService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ResolucionConcurrenteIntegrationTest {

    private static final String CORREO_TECNICO = "tecnico.concurrente@msp.gob.ec";
    private static final int TECNICOS = 4;

    @Autowired
    private SolicitudComunidadService solicitudService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void soloUnTecnicoApruebaYLosDemasRecibenElConflicto() throws Exception {
        guardarTecnico();
        Long solicitudId = solicitudService.registrarSolicitud(solicitud()).id();

        List<Throwable> errores = aprobarALaVez(solicitudId);

        assertThat(errores).hasSize(TECNICOS - 1);
        assertThat(errores).allSatisfy(error -> assertThat(error)
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Esta solicitud ya ha sido resuelta previamente por otro técnico"));
        assertThat(usuarioRepository.existsByCorreo("killa.sumak@huertos.gob.ec")).isTrue();
    }

    private List<Throwable> aprobarALaVez(Long solicitudId) throws InterruptedException {
        CountDownLatch salida = new CountDownLatch(1);
        Callable<Object> aprobar = () -> {
            salida.await();
            return solicitudService.aprobarSolicitud(solicitudId, CORREO_TECNICO);
        };
        ExecutorService hilos = Executors.newFixedThreadPool(TECNICOS);
        List<Future<Object>> resultados = new ArrayList<>();
        for (int i = 0; i < TECNICOS; i++) {
            resultados.add(hilos.submit(aprobar));
        }
        salida.countDown();
        List<Throwable> errores = new ArrayList<>();
        for (Future<Object> resultado : resultados) {
            try {
                resultado.get();
            } catch (ExecutionException e) {
                errores.add(e.getCause());
            }
        }
        hilos.shutdown();
        return errores;
    }

    private void guardarTecnico() {
        Usuario tecnico = new Usuario();
        tecnico.setNombreCompleto("Técnico concurrente");
        tecnico.setIdentificacion("1003004023");
        tecnico.setFechaNacimiento(LocalDate.of(1985, 3, 2));
        tecnico.setGenero(Genero.MASCULINO);
        tecnico.setCorreo(CORREO_TECNICO);
        tecnico.setPasswordHash("hash");
        tecnico.setRol(Rol.TECNICO_MSP);
        tecnico.setActivo(true);
        tecnico.setCreadoEn(LocalDateTime.of(2026, 11, 1, 8, 0));
        usuarioRepository.save(tecnico);
    }

    private CrearSolicitudComunidadRequest solicitud() {
        return new CrearSolicitudComunidadRequest("Huerto Killa Sumak", TipoOrganizacion.BARRIO, "Imbabura",
                "Otavalo", "Calle Bolívar y Sucre", new BigDecimal("0.2341000"), new BigDecimal("-78.2610000"),
                2550, DisenoPrincipal.EN_TIERRA, "SURCOS_WACHU", "1003004015", "Killa Sumak",
                LocalDate.of(1988, 7, 21), Genero.FEMENINO, "killa.sumak@huertos.gob.ec", "0998877665", true);
    }
}
