# Huertos Medicinales Comunitarios

Aplicación web gamificada para el registro y seguimiento de huertos medicinales comunitarios, según los lineamientos del MSP. Proyecto de ISWD652 Calidad de Software, período 2026B, Noctua Solutions.

## Requisitos

- Docker con Docker Compose
- Java 21 (solo para desarrollar el back fuera de Docker)
- Node.js 22 (solo para desarrollar el front fuera de Docker)

Maven no hace falta instalarlo: el back usa el wrapper `./mvnw`.

## Levantar todo

```bash
cp .env.example .env
docker compose up -d --build
```

| Servicio | URL |
|---|---|
| Front | http://localhost:5173 |
| API | http://localhost:8080/api |
| Swagger | http://localhost:8080/swagger-ui.html |
| Ping del API | http://localhost:8080/api/ping |
| MariaDB | localhost:3306 (usuario y clave en `.env`) |

`GET /api/ping` recorre Controller, Service, Repository y la base de datos, y responde 200 con la versión de MariaDB. Si responde, la arquitectura está conectada de punta a punta.

## Desarrollo diario

Para trabajar con recarga en caliente, deja solo la base en Docker y corre el back y el front en tu máquina:

```bash
docker compose up -d db

cd backend && ./mvnw spring-boot:run      # DevTools recarga al recompilar
cd frontend && npm install && npm run dev # Vite redirige /api al puerto 8080
```

## Pruebas

```bash
cd backend && ./mvnw verify      # JUnit 5, Mockito, Testcontainers; cobertura en target/site/jacoco
cd frontend && npm run test:run  # Vitest, Testing Library, MSW
cd frontend && npm run coverage  # con cobertura
```

Las pruebas de integración del back levantan su propia MariaDB con Testcontainers, así que necesitan Docker encendido.

## Estructura del repositorio

```
huertos-medicinales/
├── backend/                Spring Boot 4.1, Java 21, Maven
├── frontend/               React 19, TypeScript, Vite
├── docs/                   reglas de trabajo del equipo
├── .github/
│   ├── workflows/          CI del back y del front (corren solo si cambia su carpeta)
│   ├── ISSUE_TEMPLATE/     plantillas de caso de uso, defecto y tarea
│   └── pull_request_template.md
├── docker-compose.yml      MariaDB + API + front
├── .env.example
└── CONTRIBUTING.md         enlaces a las reglas de trabajo
```

## Arquitectura del back

Arquitectura en capas. Cada petición entra por el controller y baja hasta la base sin saltarse capas:

```
Controller  →  Service  →  Repository  →  MariaDB
   (dto)                    (model)
```

```
backend/src/main/java/com/huertos/
├── model/         Entidades JPA, tal como salen del diagrama de clases validado
├── repository/    Interfaces de Spring Data JPA
├── service/       Interfaces con la lógica de negocio
│   └── impl/      Implementaciones de esas interfaces
├── controller/    Endpoints REST bajo /api
├── dto/           Requests y responses; los controllers nunca exponen entidades
├── mapper/        Mappers de MapStruct entre entidades y DTOs
├── config/        Seguridad, CORS, beans generales
└── exception/     Excepciones propias y manejo global (@RestControllerAdvice)

backend/src/main/resources/
├── application.yml
└── db/migration/  Migraciones Flyway (V1__..., V2__...)
```

Reglas:

- **Nombres:** `ComunidadController`, `ComunidadService`, `ComunidadServiceImpl`, `ComunidadRepository`, `Comunidad` (entidad), `ComunidadRequest` y `ComunidadResponse` (DTOs), `ComunidadMapper`.
- **Esquema:** solo lo cambia Flyway. Hibernate está en `ddl-auto: validate`, así que si una entidad no coincide con las tablas, la aplicación no arranca. Una migración que ya está en `develop` no se edita nunca: se crea otra nueva.
- **Errores:** se lanzan excepciones del paquete `exception` y `GlobalExceptionHandler` las convierte en respuestas con formato ProblemDetail. Los controllers no arman respuestas de error a mano.
- **Seguridad:** el API es sin estado. Hoy solo son públicas `/api/ping`, la salud y Swagger; todo lo demás responde 403 hasta que se implemente la autenticación JWT (caso de uso de inicio de sesión).
- **Pruebas por capa:** Service con Mockito (sin Spring), Controller con `@WebMvcTest` y el servicio simulado, y flujos completos con `@SpringBootTest` + Testcontainers. Los ejemplos están en `PingServiceImplTest`, `PingControllerTest` y `PingIntegrationTest`.

## Arquitectura del front

```
frontend/src/
├── api/           Cliente HTTP único (client.ts); nadie más llama a fetch
├── features/      Una carpeta por caso de uso: llamadas al API, componentes y sus pruebas
├── components/    Componentes reutilizables entre features
├── pages/         Pantallas que arman features
└── test/          Configuración de Vitest y respuestas simuladas del API (MSW)
```

Cada endpoint que use el front se simula en `src/test/mocks/handlers.ts`, de modo que las pruebas del front no dependen de que el back esté levantado.

## Reglas de trabajo

- [Flujo de trabajo con Git](docs/flujo-de-trabajo-git.md)
- [Estándar de commits](docs/estandar-commits.md)
- [Estándar de GitHub Issues](docs/estandar-issues.md)
