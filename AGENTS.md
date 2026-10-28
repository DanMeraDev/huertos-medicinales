# Instrucciones para asistentes de IA

Proyecto: Huertos Medicinales Comunitarios. Monorepo con `backend/` (Java 21, Spring Boot 4.1, Spring Data JPA, Flyway, MapStruct, Lombok, MariaDB 11.4) y `frontend/` (React 19, TypeScript, Vite).

Las reglas completas están en `docs/reglas/uso-de-ia.md`. La estructura de cada parte está en `docs/backend/estructura.md`, `docs/frontend/estructura.md` y `docs/db/estructura.md`.

## Reglas obligatorias

- Escribe solo lo necesario para la tarea pedida. Nada de funcionalidades, parámetros o validaciones adicionales.
- No escribas comentarios en el código. La única excepción es el Javadoc en interfaces de `service` y en reglas de negocio no obvias.
- No dejes código sin usar: imports, variables, métodos, código comentado, `TODO`, `console.log` ni `System.out`.
- No agregues dependencias ni modifiques archivos que la tarea no requiere.
- Reutiliza lo que ya existe antes de crear algo nuevo.
- Nombres del negocio en español; sufijos técnicos en inglés (`Controller`, `Service`, `Repository`, `Request`, `Response`).
- Si una prueba falla, corrige el código, no la prueba.

## Autoría

- No agregues firmas, menciones ni atribuciones de herramientas de IA en ningún lugar: ni `Co-Authored-By`, ni "Generated with ...", ni nombres de asistentes en commits, títulos o descripciones de Pull Requests, issues, comentarios del código ni documentación.
- Los commits se hacen con la identidad de Git de la persona que trabaja; no se cambia `user.name` ni `user.email`.

## Commits y Pull Requests

Formato completo en `docs/reglas/estandar-commits.md`. Resumen:

- Conventional Commits en español: `<tipo>(<ámbito>): <descripción>`.
- Tipos: `feat`, `fix`, `test`, `refactor`, `docs`, `style`, `build`, `ci`, `chore`, `release`.
- Ámbitos: `back`, `front`, `db`, `docker`, `ci`, `docs`. Si toca back y front a la vez, sin ámbito.
- Descripción en presente y tercera persona (`agrega`, `corrige`), en minúscula, sin punto final, máximo 72 caracteres en total.
- Pie opcional: `Cierra #<número>` o `Refs #<número>`.
- El título del Pull Request sigue el mismo formato, porque se fusiona con squash.
- Ramas desde `develop`: `feature/CU-XX-descripcion`, `fix/<issue>-descripcion`, `docs/...`, `chore/...`. Nunca push directo a `main` ni a `develop`, ni `push --force` en ramas compartidas.

## Back (`backend/`, paquete `com.huertos`)

- Capas: `controller` → `service` (interfaz) / `service/impl` → `repository` → `model`. Además `dto`, `mapper`, `config`, `exception`.
- El controller no accede al repository. La API recibe y devuelve DTOs, nunca entidades.
- Inyección por constructor con `@RequiredArgsConstructor` y campos `private final`.
- DTOs como `record`. Sin `@Data` en entidades.
- `@Transactional` en el servicio.
- Errores con excepciones de `exception/`, convertidas por `GlobalExceptionHandler`.
- Cambios de esquema solo con una migración Flyway nueva en `src/main/resources/db/migration`. Nunca editar una existente ni usar `ddl-auto: update`.
- Configuración en `application.yml` y sus perfiles. Nunca crear `application.properties`.
- Endpoints documentados con `@Tag` y `@Operation` de springdoc.
- Verificación: `./mvnw verify`.

## Front (`frontend/src`)

- Carpetas: `api/`, `features/<feature>/`, `components/`, `pages/`, `test/`.
- Sin `any`. Los tipos del API replican los DTOs del back.
- Llamadas al back solo desde `api/client.ts` y los archivos `*Api.ts` de cada feature.
- Una feature no importa archivos de otra.
- Componentes como funciones con exportación con nombre.
- Cada endpoint usado en pruebas se simula en `src/test/mocks/handlers.ts`.
- Verificación: `npm run lint`, `npm run test:run` y `npm run build`.
