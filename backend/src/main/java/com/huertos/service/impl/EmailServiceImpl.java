package com.huertos.service.impl;

import com.huertos.config.CorreoProperties;
import com.huertos.dto.CredencialesEmailDto;
import com.huertos.dto.RechazoSolicitudEmailDto;
import com.huertos.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String ASUNTO_CREDENCIALES =
            "¡Bienvenido/a a la Red de Huertos Medicinales! Credenciales de tu comunidad";
    private static final String ASUNTO_RECHAZO =
            "Notificación sobre tu solicitud de huerto medicinal - Ministerio de Salud Pública";
    private static final String KINTI_CELEBRACION = "correo/kinti-celebracion.png";
    private static final String KINTI_SABIDURIA = "correo/kinti-sabiduria.png";

    private static final String PLANTILLA = """
            <!doctype html>
            <html lang="es">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"></head>
            <body style="margin:0;padding:0;background-color:#f4f7f2;font-family:Arial,Helvetica,sans-serif;color:#1d2a1f">
              <div style="width:100%%;max-width:36rem;margin:0 auto">
                <div style="background-color:#2d5a3f;color:#ffffff;padding:16px;text-align:center;font-size:20px;font-weight:bold">
                  Huertos Medicinales Comunitarios
                </div>
                <div style="padding:16px;font-size:16px;line-height:1.5">
                  <img src="cid:kinti" alt="Kinti" width="120" style="display:block;width:120px;height:auto;margin:0 auto 8px">
                  %s
                </div>
              </div>
            </body>
            </html>
            """;

    private static final String CUERPO_CREDENCIALES = """
            <p>Hola %s,</p>
            <p>¡Felicidades! La solicitud de la comunidad <strong>%s</strong> fue aprobada.</p>
            <div style="background-color:#ffffff;border:1px solid #d3ded2;border-radius:8px;padding:16px;color:#1d2a1f">
              <p style="margin:0 0 8px">Usuario: <strong>%s</strong></p>
              <p style="margin:0">Contraseña temporal: <strong>%s</strong></p>
            </div>
            <p>Por seguridad, al iniciar sesión por primera vez deberás cambiar tu contraseña.</p>
            <a href="%s" style="display:block;width:100%%;max-width:20rem;min-height:44px;line-height:44px;margin:16px auto;background-color:#2d5a3f;color:#ffffff;border-radius:8px;text-align:center;text-decoration:none;font-weight:bold">Iniciar sesión</a>
            """;

    private static final String CUERPO_RECHAZO = """
            <p>Hola %s,</p>
            <p>Revisamos con atención la solicitud de la comunidad <strong>%s</strong> y por ahora no fue aprobada.</p>
            <div style="background-color:#ffffff;border-left:4px solid #b3261e;border-radius:8px;padding:16px">
              <p style="margin:0 0 8px"><strong>Motivo indicado por el técnico del MSP:</strong></p>
              <p style="margin:0">%s</p>
            </div>
            <p>Puedes corregir lo indicado y postular nuevamente en una próxima convocatoria.</p>
            <p><a href="%s" style="color:#2d5a3f">Visitar el portal de Huertos Medicinales</a></p>
            """;

    private final JavaMailSender mailSender;
    private final CorreoProperties correoProperties;

    @Async
    @Override
    @TransactionalEventListener
    public void enviarCredencialesAprobacion(CredencialesEmailDto datos) {
        String cuerpo = CUERPO_CREDENCIALES.formatted(escapar(datos.nombreSolicitante()),
                escapar(datos.nombreComunidad()), escapar(datos.destinatario()), escapar(datos.passwordTemporal()),
                correoProperties.urlFrontend() + "/login");
        enviar(datos.destinatario(), ASUNTO_CREDENCIALES, cuerpo, KINTI_CELEBRACION, "credenciales");
    }

    @Async
    @Override
    @TransactionalEventListener
    public void enviarNotificacionRechazo(RechazoSolicitudEmailDto datos) {
        String cuerpo = CUERPO_RECHAZO.formatted(escapar(datos.nombreSolicitante()),
                escapar(datos.nombreComunidad()), escapar(datos.motivoRechazo()), correoProperties.urlFrontend());
        enviar(datos.destinatario(), ASUNTO_RECHAZO, cuerpo, KINTI_SABIDURIA, "rechazo");
    }

    private void enviar(String destinatario, String asunto, String cuerpo, String kinti, String tipo) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, true, StandardCharsets.UTF_8.name());
            ayudante.setFrom(correoProperties.remitente());
            ayudante.setTo(destinatario);
            ayudante.setSubject(asunto);
            ayudante.setText(PLANTILLA.formatted(cuerpo), true);
            ClassPathResource imagenKinti = new ClassPathResource(kinti);
            ayudante.addInline("kinti", imagenKinti.getFilename(), imagenKinti, "image/png");
            mailSender.send(mensaje);
            log.info("Correo de {} enviado a {}", tipo, destinatario);
        } catch (MailException | MessagingException falla) {
            log.warn("No se pudo enviar el correo de {} a {}: {}", tipo, destinatario, falla.getMessage());
        }
    }

    private String escapar(String texto) {
        return HtmlUtils.htmlEscape(texto, StandardCharsets.UTF_8.name());
    }
}
