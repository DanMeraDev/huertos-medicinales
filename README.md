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
| Salud del API | http://localhost:8080/actuator/health |
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
├── .github/
│   ├── workflows/          CI del back y del front (corren solo si cambia su carpeta)
│   ├── ISSUE_TEMPLATE/     plantillas de caso de uso y de defecto
│   └── pull_request_template.md
├── docker-compose.yml      MariaDB + API + front
└── .env.example
```

## Arquitectura del back

Arquitectura en capas. Cada petición entra por el controller y baja hasta la base sin saltarse capas:

```
Controller  →  Service  →  Repository  →  MariaDB
   (dto)                    (domain)
```

```
backend/src/main/java/com/noctua/huertos/
├── domain/        Entidades JPA, tal como salen del diagrama de clases validado
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

## Flujo de trabajo con Git

**Ramas**

- `main`: lo entregado. Solo recibe merges desde `develop` al cerrar un incremento, con etiqueta (`v0.1`, `v0.2`, ...).
- `develop`: integración del incremento en curso.
- Ramas de trabajo desde `develop`, cortas y con el caso de uso en el nombre:
  - `feature/CU-01-registro-comunidad`
  - `fix/BUG-07-ranking-duplicado`

**Reglas**

- Nadie hace push directo a `main` ni a `develop`.
- Todo entra por Pull Request hacia `develop`, con al menos una aprobación y el CI en verde.
- Cada issue corresponde a un caso de uso (`CU-01: Registro de comunidad`), creado con la plantilla. El PR lo cierra con `Cierra #<número>`.
- Antes de abrir el PR: `git pull origin develop` y resolver conflictos en tu rama.
- Mensajes de commit en imperativo y cortos: `Agrega validación de cédula en registro de miembro`.

**Configurar la protección de ramas (una sola vez, el dueño del repo)**

En GitHub, *Settings → Branches → Add branch ruleset*, para `main` y `develop`:

- Require a pull request before merging, con 1 aprobación
- Require status checks to pass: `Backend / test` y `Frontend / test`
- Block force pushes
