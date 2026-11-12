package com.huertos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.dto.CrearSolicitudComunidadRequest;
import com.huertos.dto.RechazarSolicitudRequest;
import com.huertos.dto.ResolucionSolicitudResponse;
import com.huertos.model.DisenoPrincipal;
import com.huertos.model.EstadoSolicitud;
import com.huertos.model.Genero;
import com.huertos.model.Rol;
import com.huertos.model.TipoOrganizacion;
import com.huertos.model.Usuario;
import com.huertos.repository.UsuarioRepository;
import com.huertos.service.SolicitudComunidadService;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class EmailIntegrationTest {

    private static final String CORREO_TECNICO = "tecnico.correo@msp.gob.ec";
    private static final String CORREO_SOLICITANTE = "pacha.mama@huertos.gob.ec";

    @Autowired
    private SolicitudComunidadService solicitudService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    void elRechazoRespondeAntesDeEnviarElCorreoEnOtroHilo() throws Exception {
        when(mailSender.createMimeMessage()).thenAnswer(invocacion -> {
            Thread.sleep(500);
            return new MimeMessage(Session.getInstance(new Properties()));
        });
        guardarTecnico();
        Long solicitudId = solicitudService.registrarSolicitud(solicitud()).id();

        long inicio = System.nanoTime();
        ResolucionSolicitudResponse respuesta = solicitudService.rechazarSolicitud(solicitudId,
                new RechazarSolicitudRequest("La ubicación indicada no tiene acceso seguro a fuente de agua."),
                CORREO_TECNICO);
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;

        assertThat(respuesta.estado()).isEqualTo(EstadoSolicitud.RECHAZADA);
        assertThat(milisegundos).isLessThan(500);
        ArgumentCaptor<MimeMessage> mensaje = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, timeout(5000)).send(mensaje.capture());
        assertThat(mensaje.getValue().getRecipients(Message.RecipientType.TO)[0].toString())
                .isEqualTo(CORREO_SOLICITANTE);
    }

    private void guardarTecnico() {
        Usuario tecnico = new Usuario();
        tecnico.setNombreCompleto("Técnica de prueba");
        tecnico.setIdentificacion("1700000099");
        tecnico.setFechaNacimiento(LocalDate.of(1985, 3, 2));
        tecnico.setGenero(Genero.FEMENINO);
        tecnico.setCorreo(CORREO_TECNICO);
        tecnico.setPasswordHash("hash");
        tecnico.setRol(Rol.TECNICO_MSP);
        tecnico.setActivo(true);
        tecnico.setCreadoEn(LocalDateTime.of(2026, 11, 1, 8, 0));
        usuarioRepository.save(tecnico);
    }

    private CrearSolicitudComunidadRequest solicitud() {
        return new CrearSolicitudComunidadRequest("Huerto Pacha Mama", TipoOrganizacion.BARRIO, "Imbabura",
                "Otavalo", "Calle Bolívar y Sucre", new BigDecimal("0.2341000"), new BigDecimal("-78.2610000"),
                2550, DisenoPrincipal.EN_TIERRA, "SURCOS_WACHU", "1003004005", "Pacha Mama Yupanqui",
                LocalDate.of(1988, 7, 21), Genero.FEMENINO, CORREO_SOLICITANTE, "0998877665", true);
    }
}
