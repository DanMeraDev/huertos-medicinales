package com.huertos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.huertos.config.CorreoProperties;
import com.huertos.dto.CredencialesEmailDto;
import com.huertos.dto.RechazoSolicitudEmailDto;
import com.huertos.service.impl.EmailServiceImpl;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    private static final String DESTINATARIO = "comunidad.allpa@huertos.gob.ec";

    @Mock
    private JavaMailSender mailSender;

    private EmailService servicio;

    @BeforeEach
    void crearServicio() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        servicio = new EmailServiceImpl(mailSender,
                new CorreoProperties("no-responder@huertos.gob.ec", "https://huertos.gob.ec/login"));
    }

    @Test
    void enviaLasCredencialesConElEnlaceAlLogin() throws Exception {
        servicio.enviarCredencialesAprobacion(new CredencialesEmailDto(DESTINATARIO, "Segundo Tránsito Amaguaña",
                "Huerto Comunitario Allpa Viva", "Tm4kQz7RwP2nXa9e"));

        MimeMessage enviado = mensajeEnviado();
        assertThat(enviado.getRecipients(Message.RecipientType.TO)[0].toString()).isEqualTo(DESTINATARIO);
        assertThat(enviado.getFrom()[0].toString()).isEqualTo("no-responder@huertos.gob.ec");
        assertThat(enviado.getSubject())
                .isEqualTo("¡Bienvenido/a a la Red de Huertos Medicinales! Credenciales de tu comunidad");
        assertThat(cuerpo(enviado))
                .contains("Segundo Tránsito Amaguaña", "Huerto Comunitario Allpa Viva", DESTINATARIO,
                        "Tm4kQz7RwP2nXa9e", "href=\"https://huertos.gob.ec/login\"", "cambiar tu contraseña",
                        "max-width:20rem", "min-height:44px");
    }

    @Test
    void enviaElRechazoConElMotivoExactoYEscapado() throws Exception {
        servicio.enviarNotificacionRechazo(new RechazoSolicitudEmailDto(DESTINATARIO, "Segundo Tránsito Amaguaña",
                "Huerto Comunitario Allpa Viva", "La ubicación no tiene <acceso> seguro a fuente de agua."));

        MimeMessage enviado = mensajeEnviado();
        assertThat(enviado.getSubject())
                .isEqualTo("Notificación sobre tu solicitud de huerto medicinal - Ministerio de Salud Pública");
        assertThat(cuerpo(enviado))
                .contains("La ubicación no tiene &lt;acceso&gt; seguro a fuente de agua.", "Huerto Comunitario Allpa Viva")
                .doesNotContain("<acceso>");
    }

    @Test
    void registraLaFallaDelServidorSinPropagarla() {
        doThrow(new MailSendException("SMTP no disponible")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> servicio.enviarNotificacionRechazo(new RechazoSolicitudEmailDto(DESTINATARIO,
                "Segundo Tránsito Amaguaña", "Huerto Comunitario Allpa Viva", "Motivo de rechazo suficientemente largo")))
                .doesNotThrowAnyException();
    }

    private MimeMessage mensajeEnviado() {
        ArgumentCaptor<MimeMessage> mensaje = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(mensaje.capture());
        return mensaje.getValue();
    }

    private String cuerpo(MimeMessage mensaje) throws Exception {
        return (String) mensaje.getContent();
    }
}
