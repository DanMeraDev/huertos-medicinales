---
name: Caso de Uso
about: Especificación de caso de uso 1:1 con el análisis (PUDS) y diseño técnico exacto para el Agente de IA.
title: "US-CU07: Enviar respuesta por correo (Credenciales o rechazo)"
labels: ["caso-de-uso", "back", "imprescindible"]
---

## 1. Información General (PUDS)

* **Código:** `US-CU07`
* **Incremento:** `Incremento 1`
* **Actor Principal:** `Motor de Gamificación y Sistema <<Sistema>>`
* **Descripción:** `Caso de uso de backend que opera en segundo plano (sin interfaz de usuario dedicada). Se encarga del despacho asíncrono de correos electrónicos transaccionales al solicitante tras la resolución de su propuesta comunitaria en US-CU05. Si la solicitud es aprobada, emite un correo formal de bienvenida con las credenciales de acceso iniciales (correo y contraseña temporal) e indicaciones para el cambio forzoso de clave (US-CU02). Si la propuesta es rechazada, emite un correo respetuoso con el motivo detallado por el Técnico MSP y pautas para subsanar y postular en futuras convocatorias.`
* **Dependencias:** `Depende de #US-CU05 (evento de resolución), #US-CU03 (generación de credenciales) y #US-CU04 (correo del solicitante)`

### Precondiciones
1. `El Técnico MSP ha completado con éxito la resolución de una solicitud comunitaria (aprobada o rechazada) en US-CU05.`
2. `El solicitante cuenta con un correo_solicitante válido registrado en la tabla solicitudes_comunidad.`
3. `El servicio de correo (JavaMailSender / EmailService) se encuentra habilitado y configurado en application.yml con el perfil correspondiente.`

### Postcondiciones
1. `Si fue APROBADA: se despacha un correo electrónico con plantilla HTML andina conteniendo el enlace a /login, nombre de la comunidad, usuario y contraseña temporal generada.`
2. `Si fue RECHAZADA: se despacha un correo electrónico notificando el motivo_rechazo ingresado por el técnico y sugerencias de mejora.`
3. `Se registra en los logs de la aplicación el resultado del despacho de correo (éxito o advertencia de entrega) sin afectar la transacción de base de datos de US-CU05.`

---

## 2. Escenario Básico (Flujo Principal Actor - Sistema)

1. El **Sistema** (a través de `SolicitudComunidadServiceImpl`) publica un evento interno de dominio o invoca de forma asíncrona (`@Async`) a `NotificacionCorreoService` al concluir la resolución de la solicitud en `US-CU05`.
2. El **Sistema** determina la plantilla de correo según el estado de la resolución:
   - **Caso de Aprobación:**
     - `Destinatario`: `correoSolicitante`.
     - `Asunto`: *"¡Bienvenido/a a la Red de Huertos Medicinales! Credenciales de tu comunidad"*.
     - `Cuerpo del correo`: Mensaje de felicitación con saludo andino, nombre de la comunidad aprobada (`nombreComunidad`), usuario de acceso (`correoSolicitante`), contraseña temporal en texto claro recién generada en `US-CU03`, aviso de cambio de contraseña obligatorio en el primer inicio de sesión (`US-CU02`), y botón/enlace hacia la URL de acceso pública (`/login`).
   - **Caso de Rechazo:**
     - `Destinatario`: `correoSolicitante`.
     - `Asunto`: *"Notificación sobre tu solicitud de huerto medicinal - Ministerio de Salud Pública"*.
     - `Cuerpo del correo`: Saludo respetuoso, referencia a la propuesta comunitaria recibida, detalle explícito del `motivoRechazo` redactado por el Técnico MSP.
3. El **Sistema** construye el mensaje MIME con codificación `UTF-8` y formato HTML estilizado con la identidad gráfica del proyecto.
4. El **Sistema** entrega el mensaje al servidor de correo configurado (`JavaMailSender`).
5. El **Sistema** registra en el log de auditoría el identificador del mensaje despachado, destinatario y tipo de notificación enviada.

---

## 3. Escenarios Alternativos (Excepciones y Reglas de Negocio)

* **4a. Falla de conexión con el servidor de correo o timeout SMTP:**
  - **Condición:** Si el servidor de correos no responde, rechaza la conexión o expira el tiempo de espera.
  - **Comportamiento:** Como el despacho se ejecuta de manera asíncrona (`@Async`) y aislada de la transacción principal, la falla de entrega no revierte la aprobación/rechazo de la solicitud ni la creación de usuarios o comunidades. El sistema captura la excepción `MailException`, registra un log de error (`ERROR` / `WARN`) para posterior reintento o auditoría, y la API de `US-CU05` responde con éxito al técnico.
* **4b. Dirección de correo inexistente o rebotada (Mail Bounced):**
  - **Condición:** Si la dirección de correo proporcionada en la solicitud no puede recibir mensajes.
  - **Comportamiento:** Se registra la alerta en logs y el estado de la solicitud permanece inmutable en la base de datos.

---

## 4. Análisis de Robustez y Diseño Técnico (Contrato Estricto para IA)

### 4.1. Objetos Boundary (Frontend React - `frontend/src/`)
- *Nota arquitectural:* Este caso de uso **no posee interfaz gráfica propia ni componentes visuales en React**.
- Su impacto visual se manifiesta de manera externa e indirecta: el usuario final recibe el correo en su bandeja de entrada y, al hacer clic en el enlace adjunto, accede a la pantalla pública de `LoginPage.tsx` (`US-CU01`) del frontend.

### 4.2. Objetos Control (Backend Spring Boot - `backend/src/main/java/com/huertos/`)
- [x] **Controller / Endpoints:**
  - *No expone endpoint REST.* Se ejecuta automáticamente mediante eventos de Spring (`@EventListener` / `ApplicationEventPublisher`) o llamada directa asíncrona desde `SolicitudComunidadServiceImpl.java`.
- [x] **DTOs / Modelos de Notificación (`com.huertos.dto`):**
  - `CredencialesEmailDto.java`: `public record CredencialesEmailDto(String destinatario, String nombreSolicitante, String nombreComunidad, String correoUsuario, String passwordTemporal, String urlLogin) {}`.
  - `RechazoSolicitudEmailDto.java`: `public record RechazoSolicitudEmailDto(String destinatario, String nombreSolicitante, String nombreComunidad, String motivoRechazo) {}`.
- [x] **Service (`com.huertos.service.EmailService.java` y `com.huertos.service.impl.EmailServiceImpl.java`):**
  - Método `@Async void enviarCredencialesAprobacion(CredencialesEmailDto datos)`.
  - Método `@Async void enviarNotificacionRechazo(RechazoSolicitudEmailDto datos)`.
  - Inyección de `org.springframework.mail.javamail.JavaMailSender` y `org.springframework.core.env.Environment` o configuración de URLs base.
- [x] **Configuración de Correo (`com.huertos.config.MailConfig.java` o `application.yml`):**
  - Parámetros `spring.mail.host`, `spring.mail.port`, `spring.mail.username`, `spring.mail.password`.
  - Habilitación de ejecución asíncrona mediante `@EnableAsync` en clase de configuración.

### 4.3. Objetos Entity y Base de Datos (`backend/src/main/resources/db/migration/` y `com.huertos`)
- [x] **Migración Flyway:**
  - No altera tablas ni esquema de base de datos. Consume datos de `solicitudes_comunidad` y `usuarios`.
  - *(NO generar nuevo archivo de migración SQL)*.
- [x] **Entidad JPA:**
  - No requiere entidad JPA propia.

---

## 5. Criterios de Aceptación y Pruebas Requeridas (CI/CD)

- [x] **Prueba unitaria de Servicio de Correo (`backend/src/test/java/com/huertos/service/EmailServiceImplTest.java`):**
  - Con JUnit 5 y Mockito (`@Mock JavaMailSender`), verificando que al llamar `enviarCredencialesAprobacion(...)` se construya y despache un mensaje MIME con el destinatario, asunto y cuerpo HTML correctos.
  - Verificación de que `enviarNotificacionRechazo(...)` incluya con exactitud el motivo de rechazo ingresado.
  - Comprobación de que ante un fallo de `JavaMailSender.send(...)` (`MailSendException`), la excepción sea capturada y registrada sin propagarse al hilo principal.
- [x] **Prueba de Integración con Mock de Correo (`backend/src/test/java/com/huertos/service/EmailIntegrationTest.java`):**
  - Verificación con `@SpringBootTest` asegurando que la llamada asíncrona se procese sin bloquear la respuesta de la transacción en `US-CU05`.
