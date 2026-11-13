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
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
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
                new CorreoProperties("no-responder@huertos.gob.ec", "https://huertos.gob.ec"));
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
                .contains("La ubicación no tiene &lt;acceso&gt; seguro a fuente de agua.", "Huerto Comunitario Allpa Viva",
                        "href=\"https://huertos.gob.ec\"")
                .doesNotContain("<acceso>");
    }

    @Test
    void incluyeAKintiCelebrandoEnElCorreoDeAprobacion() throws Exception {
        servicio.enviarCredencialesAprobacion(new CredencialesEmailDto(DESTINATARIO, "Segundo Tránsito Amaguaña",
                "Huerto Comunitario Allpa Viva", "Tm4kQz7RwP2nXa9e"));

        MimeMessage enviado = mensajeEnviado();
        assertThat(cuerpo(enviado)).contains("src=\"cid:kinti\"", "alt=\"Kinti\"");
        BodyPart kinti = imagenIncrustada(enviado);
        assertThat(kinti.getFileName()).isEqualTo("kinti-celebracion.png");
        assertThat(kinti.getContentType()).startsWith("image/png");
    }

    @Test
    void incluyeAKintiEnElCorreoDeRechazo() throws Exception {
        servicio.enviarNotificacionRechazo(new RechazoSolicitudEmailDto(DESTINATARIO, "Segundo Tránsito Amaguaña",
                "Huerto Comunitario Allpa Viva", "Motivo de rechazo suficientemente largo"));

        MimeMessage enviado = mensajeEnviado();
        assertThat(cuerpo(enviado)).contains("src=\"cid:kinti\"");
        assertThat(imagenIncrustada(enviado).getFileName()).isEqualTo("kinti-sabiduria.png");
    }

    @Test
    void registraLaFallaDelServidorSinPropagarla() {
        doThrow(new MailSendException("SMTP no disponible")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> servicio.enviarNotificacionRechazo(new RechazoSolicitudEmailDto(DESTINATARIO,
                "Segundo Tránsito Amaguaña", "Huerto Comunitario Allpa Viva", "Motivo de rechazo suficientemente largo")))
                .doesNotThrowAnyException();
    }

    private MimeMessage mensajeEnviado() throws Exception {
        ArgumentCaptor<MimeMessage> mensaje = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(mensaje.capture());
        mensaje.getValue().saveChanges();
        return mensaje.getValue();
    }

    private String cuerpo(MimeMessage mensaje) throws Exception {
        for (BodyPart parte : partes(mensaje)) {
            if (parte.isMimeType("text/html")) {
                return (String) parte.getContent();
            }
        }
        throw new AssertionError("El correo no tiene cuerpo HTML");
    }

    private BodyPart imagenIncrustada(MimeMessage mensaje) throws Exception {
        for (BodyPart parte : partes(mensaje)) {
            String[] identificadores = parte.getHeader("Content-ID");
            if (parte.isMimeType("image/png") && identificadores != null && identificadores[0].equals("<kinti>")) {
                return parte;
            }
        }
        throw new AssertionError("El correo no incluye a Kinti");
    }

    private List<BodyPart> partes(Part parte) throws Exception {
        List<BodyPart> encontradas = new ArrayList<>();
        if (parte.getContent() instanceof Multipart multiparte) {
            for (int i = 0; i < multiparte.getCount(); i++) {
                BodyPart hija = multiparte.getBodyPart(i);
                encontradas.add(hija);
                encontradas.addAll(partes(hija));
            }
        }
        return encontradas;
    }
}
