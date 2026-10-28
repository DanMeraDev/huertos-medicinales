# Huertos Medicinales Comunitarios

Aplicación web gamificada para el registro y seguimiento de huertos medicinales comunitarios, según los lineamientos del MSP. Proyecto de ISWD652 Calidad de Software, período 2026B, Noctua Solutions.

## Inicio rápido

Con Docker Desktop abierto:

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

Para instalar las herramientas en Windows y trabajar en local, sigue [Primeros pasos](docs/primeros-pasos.md).

## Tecnologías

| Parte | Tecnologías |
|---|---|
| Back | Java 21, Spring Boot 4.1, Spring Data JPA, Spring Security, Flyway, springdoc (Swagger), MapStruct, Lombok |
| Front | React 19, TypeScript, Vite |
| Base de datos | MariaDB 11.4 |
| Pruebas | JUnit 5, Mockito, Testcontainers, JaCoCo, Vitest, Testing Library, MSW |
| Infraestructura | Docker Compose, GitHub Actions |

## Estructura del repositorio

```
huertos-medicinales/
├── backend/                Spring Boot (ver docs/backend/estructura.md)
├── frontend/               React + Vite (ver docs/frontend/estructura.md)
├── docs/                   documentación del proyecto
├── .github/
│   ├── workflows/          CI del back y del front (corren solo si cambia su carpeta)
│   ├── ISSUE_TEMPLATE/     plantillas de caso de uso, defecto y tarea
│   └── pull_request_template.md
├── docker-compose.yml      MariaDB + API + front
├── .env.example
└── CONTRIBUTING.md         enlaces a las reglas de trabajo
```

## Documentación

Todo está en [`docs/`](docs/README.md):

| Área | Estructura | Ejecución | Errores comunes |
|---|---|---|---|
| Back | [estructura](docs/backend/estructura.md) | [ejecución](docs/backend/ejecucion.md) | [errores](docs/backend/errores-comunes.md) |
| Front | [estructura](docs/frontend/estructura.md) | [ejecución](docs/frontend/ejecucion.md) | [errores](docs/frontend/errores-comunes.md) |
| Base de datos | [estructura](docs/db/estructura.md) | [ejecución](docs/db/ejecucion.md) | [errores](docs/db/errores-comunes.md) |

Reglas de trabajo: [flujo con Git](docs/reglas/flujo-de-trabajo-git.md) · [commits](docs/reglas/estandar-commits.md) · [GitHub Issues](docs/reglas/estandar-issues.md)
