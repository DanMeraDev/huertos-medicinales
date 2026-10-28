# Estructura del back

API REST en Spring Boot 4.1 con Java 21, organizada en capas. Este documento explica qué va en cada carpeta, cómo se nombran los archivos y qué archivos se crean al implementar un caso de uso.

Relacionados: [Ejecución del back](ejecucion.md) · [Errores comunes del back](errores-comunes.md) · [Estructura de la base de datos](../db/estructura.md)

## 1. Vista general

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/huertos/
│   │   │   ├── HuertosApiApplication.java   punto de entrada
│   │   │   ├── controller/                  endpoints REST
│   │   │   ├── service/                     interfaces de lógica de negocio
│   │   │   │   └── impl/                    implementaciones
│   │   │   ├── repository/                  acceso a datos
│   │   │   ├── model/                       entidades JPA
│   │   │   ├── dto/                         requests y responses de la API
│   │   │   ├── mapper/                      conversión entre entidades y DTOs
│   │   │   ├── config/                      seguridad, CORS, beans generales
│   │   │   └── exception/                   excepciones y manejo global de errores
│   │   └── resources/
│   │       ├── application.yml              configuración común
│   │       ├── application-dev.yml          perfil de desarrollo
│   │       ├── application-prod.yml         perfil de producción
│   │       └── db/migration/                migraciones Flyway
│   └── test/
│       ├── java/com/huertos/                pruebas, con los mismos paquetes que main
│       └── resources/
│           └── application-test.yml         perfil de pruebas
├── .mvn/wrapper/                            configuración del Maven Wrapper
├── mvnw, mvnw.cmd                           Maven Wrapper (Linux/macOS y Windows)
├── pom.xml                                  dependencias y plugins
└── Dockerfile                               imagen del back para docker-compose
```

## 2. Flujo de una petición

Cada petición entra por el controller y baja capa por capa hasta la base. Ninguna capa se salta a otra.

```
Cliente (front)
     │  JSON
     ▼
Controller ── recibe un DTO Request, lo valida y llama al servicio
     │
     ▼
Service ───── aplica las reglas de negocio y abre la transacción
     │
     ▼
Repository ── lee y guarda entidades
     │
     ▼
MariaDB
```

La respuesta sube por el mismo camino: el servicio convierte la entidad a un DTO Response con el mapper, y el controller la devuelve como JSON.

El endpoint `GET /api/ping` ya recorre este flujo completo y sirve de referencia: `PingController` → `PingService` → `PingRepository` → MariaDB.

## 3. Paquetes

### `controller/`

Expone los endpoints REST bajo `/api`.

- **Va aquí:** recibir la petición, validar el DTO con `@Valid`, llamar al servicio y elegir el código HTTP.
- **No va aquí:** reglas de negocio, consultas a la base ni construcción manual de respuestas de error.
- **Reglas:** recibe y devuelve DTOs, nunca entidades. Cada clase lleva `@Tag` y cada método `@Operation`, para Swagger.
- **Nombre:** `<Recurso>Controller`, con la ruta en plural: `ComunidadController` → `/api/comunidades`.

### `service/` y `service/impl/`

La lógica de negocio: el cálculo de puntos, el paso de nivel, la validación de la fase lunar.

- **`service/`:** interfaces que definen qué puede hacer cada servicio. Son las que usan los controllers y las que se simulan en las pruebas.
- **`service/impl/`:** implementaciones con `@Service`.
- **Reglas:** los métodos que escriben en la base llevan `@Transactional`, y los de solo lectura `@Transactional(readOnly = true)`. Un servicio puede usar otros servicios y varios repositorios. Si no encuentra algo, lanza una excepción de `exception/`.
- **Nombre:** `<Recurso>Service` y `<Recurso>ServiceImpl`.

### `repository/`

Acceso a datos.

- **Reglas:** son interfaces que extienden `JpaRepository<Entidad, Long>`. Las consultas sencillas se declaran por nombre de método (`findByProvincia`); las complejas, con `@Query`. Solo los servicios usan repositorios.
- **Nombre:** `<Entidad>Repository`.

`PingRepository` es una excepción: usa `JdbcTemplate` porque no tiene entidad.

### `model/`

Entidades JPA, que salen del **diagrama de clases validado** del incremento.

- **Reglas:** cada entidad corresponde a una tabla creada por una migración Flyway (ver [estructura de la base](../db/estructura.md)). Hibernate no crea tablas: si la entidad no coincide con la tabla, el back no arranca. Las enumeraciones del dominio (por ejemplo `TipoActividad`) también van aquí.
- **Nombre:** sustantivo en singular: `Comunidad`, `Miembro`, `Actividad`.

### `dto/`

Objetos que viajan por la API. Separan lo que ve el front de cómo se guarda en la base.

- **Reglas:** se escriben como `record`. Los Request llevan las validaciones (`@NotBlank`, `@Size`, `@Email`); los Response, solo los datos que el front necesita. Nunca se devuelve una entidad.
- **Nombre:** `<Recurso>Request` y `<Recurso>Response`. Si hacen falta variantes: `ComunidadResumenResponse`.

### `mapper/`

Interfaces de MapStruct que convierten entre entidades y DTOs. MapStruct genera la implementación al compilar.

- **Nombre:** `<Entidad>Mapper`.

### `config/`

Configuración de Spring: seguridad, CORS, beans generales.

- `SecurityConfig`: rutas públicas y protegidas, y CORS. El filtro JWT se agrega aquí con el caso de uso de autenticación.
- `ClockConfig`: reloj inyectable para que las pruebas fijen la fecha.

### `exception/`

- **Excepciones propias:** por ejemplo `RecursoNoEncontradoException`. Se lanzan desde los servicios.
- **`GlobalExceptionHandler`:** las convierte en respuestas con formato ProblemDetail (RFC 9457), para que el front reciba siempre la misma estructura de error.

Para un error nuevo, crea la excepción y agrega su `@ExceptionHandler` en `GlobalExceptionHandler`.

## 4. Ejemplo: archivos de un caso de uso

> El código es solo ilustrativo, para mostrar dónde va cada pieza. Los campos reales salen del diagrama de clases.

Para implementar *CU-01: Registro de comunidad* se crean:

```
backend/src/main/
├── java/com/huertos/
│   ├── controller/ComunidadController.java
│   ├── service/ComunidadService.java
│   ├── service/impl/ComunidadServiceImpl.java
│   ├── repository/ComunidadRepository.java
│   ├── model/Comunidad.java
│   ├── dto/ComunidadRequest.java
│   ├── dto/ComunidadResponse.java
│   └── mapper/ComunidadMapper.java
└── resources/db/migration/V1__crear_tabla_comunidad.sql

backend/src/test/java/com/huertos/
├── service/ComunidadServiceImplTest.java
├── controller/ComunidadControllerTest.java
└── ComunidadIntegrationTest.java
```

**Entidad**

```java
@Entity
@Table(name = "comunidad")
@Getter
@Setter
@NoArgsConstructor
public class Comunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String contactoResponsable;
}
```

**Repository**

```java
public interface ComunidadRepository extends JpaRepository<Comunidad, Long> {

    boolean existsByNombre(String nombre);
}
```

**DTOs**

```java
public record ComunidadRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank String contactoResponsable) {
}

public record ComunidadResponse(Long id, String nombre, String contactoResponsable) {
}
```

**Mapper**

```java
@Mapper(componentModel = "spring")
public interface ComunidadMapper {

    @Mapping(target = "id", ignore = true)
    Comunidad toEntity(ComunidadRequest request);

    ComunidadResponse toResponse(Comunidad comunidad);
}
```

**Service**

```java
public interface ComunidadService {

    ComunidadResponse registrar(ComunidadRequest request);

    ComunidadResponse buscarPorId(Long id);
}
```

```java
@Service
@RequiredArgsConstructor
public class ComunidadServiceImpl implements ComunidadService {

    private final ComunidadRepository comunidadRepository;
    private final ComunidadMapper comunidadMapper;

    @Override
    @Transactional
    public ComunidadResponse registrar(ComunidadRequest request) {
        Comunidad comunidad = comunidadRepository.save(comunidadMapper.toEntity(request));
        return comunidadMapper.toResponse(comunidad);
    }

    @Override
    @Transactional(readOnly = true)
    public ComunidadResponse buscarPorId(Long id) {
        return comunidadRepository.findById(id)
                .map(comunidadMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la comunidad " + id));
    }
}
```

**Controller**

```java
@Tag(name = "Comunidades")
@RestController
@RequestMapping("/api/comunidades")
@RequiredArgsConstructor
public class ComunidadController {

    private final ComunidadService comunidadService;

    @Operation(summary = "Registra una comunidad")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComunidadResponse registrar(@Valid @RequestBody ComunidadRequest request) {
        return comunidadService.registrar(request);
    }

    @Operation(summary = "Obtiene una comunidad por su id")
    @GetMapping("/{id}")
    public ComunidadResponse buscarPorId(@PathVariable Long id) {
        return comunidadService.buscarPorId(id);
    }
}
```

## 5. Pruebas

Las pruebas viven en `src/test/java/com/huertos/`, con los **mismos paquetes** que el código que prueban. Hay tres niveles:

| Nivel | Qué prueba | Herramientas | Ejemplo en el repo |
|---|---|---|---|
| Unitaria de servicio | Reglas de negocio, sin Spring ni base | JUnit 5, Mockito, AssertJ | `service/PingServiceImplTest` |
| Controller | Rutas, validaciones, códigos HTTP y seguridad, con el servicio simulado | `@WebMvcTest`, MockMvc, `@MockitoBean` | `controller/PingControllerTest` |
| Integración | El flujo completo contra una MariaDB real | `@SpringBootTest`, Testcontainers | `PingIntegrationTest` |

- **Nombre:** `<Clase>Test` para unitarias y de controller; `<Recurso>IntegrationTest` para integración.
- **Métodos:** describen el comportamiento esperado, por ejemplo `registrarRechazaNombreVacio()`.
- **Base de pruebas:** las de integración importan `TestcontainersConfiguration`, que levanta MariaDB 11.4 en un contenedor desechable. No usan la base de `docker compose`.

## 6. Configuración (`application.yml`)

### 6.1 YAML en lugar de `application.properties`

La configuración de Spring Boot está en archivos **`application.yml`**. Cumplen la misma función que `application.properties`, en otro formato: YAML agrupa las propiedades por niveles y se lee mejor cuando son muchas.

```properties
# application.properties (NO se usa en este proyecto)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

```yaml
# application.yml (equivalente)
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
```

> **No crees un `application.properties`.** Si existen los dos formatos, Spring carga ambos, y cuando una propiedad está en los dos gana la del `.properties`. El resultado son valores que "no cambian" aunque edites el `.yml`. Muchos tutoriales y asistentes de IA lo crean por costumbre: si aparece, se pasa su contenido al `.yml` y se borra.

### 6.2 Archivos

| Archivo | Contenido |
|---|---|
| `application.yml` | Lo común a todos los entornos: Flyway, `ddl-auto: validate`, puerto, Actuator, Swagger |
| `application-dev.yml` | Desarrollo: lee el `.env`, muestra el SQL, log `DEBUG` |
| `application-prod.yml` | Producción: sin valores por defecto, Swagger apagado |
| `application-test.yml` (en `test/resources`) | Pruebas: log reducido; la base la pone Testcontainers |
| `db/migration/` | Migraciones Flyway; ver [estructura de la base](../db/estructura.md) |

Spring carga primero `application.yml` y después el archivo del perfil activo, que **sobrescribe** las propiedades repetidas. Los perfiles se explican en [Ejecución del back](ejecucion.md#6-perfiles).

### 6.3 Dónde va una propiedad nueva

| Si el valor... | Va en |
|---|---|
| Es igual en todos los entornos | `application.yml` |
| Cambia entre desarrollo y producción | `application-dev.yml` y `application-prod.yml` |
| Solo afecta a las pruebas | `src/test/resources/application-test.yml` |
| Es una clave, contraseña o token | **En ningún archivo.** Se lee de una variable de entorno: `${NOMBRE_VARIABLE}` |
| Depende de cada integrante (puertos, datos locales) | En el `.env`, y en el `.yml` se lee con `${VARIABLE:valor_por_defecto}` |

Las propiedades propias del proyecto van bajo el prefijo `app:`, para no confundirlas con las de Spring:

```yaml
app:
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:5173}
  gamificacion:
    puntos-por-actividad: 10
```

### 6.4 Leer una propiedad desde el código

Para un valor suelto, con `@Value`, como en `SecurityConfig`:

```java
@Value("${app.cors.allowed-origins}") List<String> origins
```

Para un grupo de valores relacionados, con un `record` y `@ConfigurationProperties`:

```java
@ConfigurationProperties(prefix = "app.gamificacion")
public record GamificacionProperties(int puntosPorActividad) {
}
```

Se registra con `@ConfigurationPropertiesScan` en `HuertosApiApplication` y se inyecta por constructor como cualquier otro bean. Spring convierte `puntos-por-actividad` en `puntosPorActividad`.

### 6.5 Cambiar un valor sin editar archivos

Cualquier propiedad se puede sobrescribir con una variable de entorno: se escribe en mayúsculas, con `_` en lugar de puntos y guiones. Por ejemplo, `spring.jpa.show-sql` se convierte en `SPRING_JPA_SHOW_SQL=false`.

Prioridad, de mayor a menor:

1. Variables de entorno del sistema, del IDE o de `docker-compose.yml`.
2. El `.env` de la raíz, solo con el perfil `dev`.
3. El archivo del perfil activo (`application-dev.yml`).
4. `application.yml`.

### 6.6 Errores frecuentes de YAML

- **La sangría importa:** se usan **2 espacios** por nivel y **nunca tabulaciones**. Una sangría mal puesta mueve la propiedad a otro grupo y Spring la ignora sin avisar.
- **Después de `:` va un espacio:** `port: 8080`, no `port:8080`.
- **Comillas** para valores con caracteres especiales como `*`, `#` o `:` al inicio: `include: "*"`.
- **Una propiedad, un lugar:** si la misma clave aparece dos veces en el mismo archivo, el back no arranca y el log muestra `found duplicate key` con el nombre de la clave.

## 7. Convenciones de código

- **Inyección por constructor** con `@RequiredArgsConstructor` de Lombok y campos `private final`. No se usa `@Autowired` sobre campos.
- **Lombok:** en entidades, `@Getter`, `@Setter` y `@NoArgsConstructor`. Se evita `@Data` en entidades, porque su `equals` y `hashCode` dan problemas con las relaciones JPA.
- **Nombres en español** para clases, métodos y variables del negocio (`registrar`, `buscarPorId`). Los sufijos técnicos van en inglés (`Controller`, `Service`, `Repository`, `Request`, `Response`).
- **Javadoc** en las interfaces de `service` y en las reglas de negocio que no son obvias. No hace falta en getters ni en métodos cuyo nombre ya lo explica.
