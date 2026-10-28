# Ejecución del back

Cómo correr el back en tu máquina desde la terminal, IntelliJ o VS Code, cómo correr sus pruebas y qué perfiles tiene.

Antes, completa [Primeros pasos](../primeros-pasos.md): JDK 21, `JAVA_HOME`, Docker Desktop y el `.env`.

Relacionados: [Estructura del back](estructura.md) · [Errores comunes del back](errores-comunes.md)

## 1. Levantar la base

El back necesita la base corriendo. Desde la raíz del repo:

```powershell
docker compose up -d db
```

Si antes levantaste todo con `docker compose up`, apaga el back de Docker para liberar el puerto 8080:

```powershell
docker compose stop api
```

## 2. Correr el back

### Desde la terminal

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Está listo cuando aparece `Started HuertosApiApplication`. Pruébalo en http://localhost:8080/api/ping.

Para detenerlo: `Ctrl+C`.

### Desde IntelliJ IDEA

1. *File → Open*, selecciona el archivo `backend\pom.xml` y elige **Open as Project**.
2. Espera a que termine de cargar Maven (barra inferior).
3. En *File → Project Structure → Project → SDK*, elige **21**. Si no aparece, usa *Add SDK → JDK* y selecciona la carpeta de Temurin 21.
4. Si IntelliJ pregunta por **annotation processing**, que lo necesita Lombok, acepta. Si no pregunta, actívalo en *Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing*.
5. Abre `HuertosApiApplication.java` y pulsa el triángulo verde junto a `main`.

### Desde VS Code

1. Instala las extensiones **Extension Pack for Java** y **Spring Boot Extension Pack**.
2. *File → Open Folder* y abre la **raíz del repo**.
3. Espera a que termine de cargar el proyecto Java (abajo a la izquierda).
4. Si tienes varios JDK: `Ctrl+Shift+P` → *Java: Configure Java Runtime*, y elige 21 para el proyecto.
5. Abre `HuertosApiApplication.java` y pulsa **Run** sobre el método `main`, o usa el panel **Spring Boot Dashboard**.

### Variables de entorno

En los dos IDE el `.env` de la raíz se lee solo, así que no hay que agregar variables a la configuración de ejecución. Funciona tanto si el IDE ejecuta desde `backend\` (IntelliJ) como desde la raíz (VS Code).

Si necesitas una variable solo para ti, sin tocar el `.env`:

- **IntelliJ:** *Run → Edit Configurations → HuertosApiApplication → Environment variables*, por ejemplo `API_PORT=8081`.
- **VS Code:** en `.vscode/launch.json`, agrega `"env": { "API_PORT": "8081" }` a la configuración de ejecución.

### Recarga en caliente

El proyecto incluye Spring Boot DevTools, que reinicia el back al recompilar:

- **IntelliJ:** *Build → Build Project* (`Ctrl+F9`) después de guardar.
- **VS Code:** recompila solo al guardar.

## 3. Direcciones útiles

| Qué | URL |
|---|---|
| Ping (recorre todas las capas hasta la base) | http://localhost:8080/api/ping |
| Swagger | http://localhost:8080/swagger-ui.html |
| Contrato OpenAPI en JSON | http://localhost:8080/v3/api-docs |
| Salud | http://localhost:8080/actuator/health |

## 4. Pruebas

```powershell
cd backend
.\mvnw.cmd verify
```

- **Docker Desktop abierto:** es obligatorio, porque las pruebas de integración levantan su propia MariaDB con Testcontainers. No usan la base de `docker compose` ni tus datos.
- **Cobertura:** el reporte de JaCoCo queda en `backend\target\site\jacoco\index.html`.
- **Una sola clase de prueba:** `.\mvnw.cmd test -Dtest=PingServiceImplTest`.
- **Desde el IDE:** clic derecho en la carpeta `src\test\java`, y luego *Run Tests* (IntelliJ) o *Run Tests* en la vista **Testing** (VS Code).

## 5. Javadoc

```powershell
cd backend
.\mvnw.cmd javadoc:javadoc
start target\reports\apidocs\index.html
```

## 6. Perfiles

| Perfil | Cuándo se usa | Cómo se activa | Diferencias |
|---|---|---|---|
| `dev` | Desarrollo local y `docker compose up` | Por defecto | Lee el `.env`, muestra el SQL en consola, log `DEBUG` de `com.huertos`, Swagger activo, detalle completo en `/actuator/health` |
| `test` | Pruebas automáticas (`mvnw verify` y CI) | Automático al correr las pruebas | La base la levanta Testcontainers y el log es reducido |
| `prod` | Entorno desplegado | `SPRING_PROFILES_ACTIVE=prod` | Sin valores por defecto: si falta `DB_URL`, `DB_USER`, `DB_PASSWORD` o `CORS_ORIGINS`, no arranca. Swagger apagado (se enciende con `SWAGGER_ENABLED=true`) y errores sin detalles internos |

Para levantar el back de Docker con `prod`, cambia `SPRING_PROFILES_ACTIVE` en el `.env` y ejecuta `docker compose up -d api`.
