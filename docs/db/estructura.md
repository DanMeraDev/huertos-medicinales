# Estructura de la base de datos

La base es **MariaDB 11.4**. Su esquema lo controla únicamente **Flyway**: cada cambio es un archivo SQL versionado en el repo. Este documento explica dónde van las migraciones, cómo se nombran y qué convenciones siguen las tablas.

Relacionados: [Ejecución de la base](ejecucion.md) · [Errores comunes de la base](errores-comunes.md) · [Estructura del back](../backend/estructura.md)

## 1. Dónde está cada cosa

```
backend/src/main/resources/
└── db/migration/
    ├── V1__crear_tabla_comunidad.sql
    ├── V2__crear_tabla_miembro.sql
    ├── V3__insertar_catalogo_tipo_suelo.sql
    └── ...
```

| Pieza | Dónde | Quién la controla |
|---|---|---|
| Tablas, columnas, índices, claves | `db/migration/*.sql` | Flyway, al arrancar el back |
| Datos de catálogo (tipos de suelo, plantas iniciales del MSP) | `db/migration/*.sql`, como `INSERT` | Flyway |
| Entidades que mapean las tablas | `backend/.../model/` | Hibernate, solo para validar |
| Historial de migraciones aplicadas | Tabla `flyway_schema_history` | Flyway (no se toca a mano) |

Hibernate está en `ddl-auto: validate`: **no crea ni modifica tablas**. Al arrancar, compara las entidades con las tablas y, si no coinciden, el back no arranca. Así nadie depende de una base que Hibernate armó a su manera en su máquina.

## 2. Cómo funciona Flyway

1. Al arrancar el back, Flyway revisa `flyway_schema_history` para saber qué migraciones ya se aplicaron.
2. Ejecuta, en orden de versión, las que faltan.
3. Guarda cada una con un *checksum*, una huella de su contenido.
4. Si una migración ya aplicada cambió de contenido, el checksum no coincide y el back se detiene.

Por eso la regla principal es: **una migración que ya está en `develop` no se edita nunca**. Para corregirla, se crea una migración nueva.

## 3. Nombre de las migraciones

```
V<número>__<descripcion_en_snake_case>.sql
```

- **Prefijo:** `V` mayúscula.
- **Separador:** **dos** guiones bajos después del número.
- **Descripción:** en minúsculas, con guion bajo entre palabras, sin tildes ni ñ.
- **Número:** el siguiente al último que hay en `develop`. Antes de crear el archivo, actualiza tu rama (`git pull origin develop`) para no repetirlo.

| Bien | Mal | Por qué está mal |
|---|---|---|
| `V4__crear_tabla_huerto.sql` | `V4_crear_tabla_huerto.sql` | Un solo guion bajo |
| `V5__agregar_columna_area_a_huerto.sql` | `v5__huerto.sql` | Minúscula y descripción vaga |
| `V6__insertar_catalogo_tipo_abono.sql` | `V6__insertar catálogo.sql` | Espacio y tilde |

**Si dos personas usan el mismo número:** el CI falla con `Found more than one migration with version`. Quien fusiona después renombra su archivo al siguiente número libre, antes de fusionar.

**Una migración, un propósito:** crear una tabla, agregar una columna o cargar un catálogo. No se mezclan cambios de varios casos de uso en el mismo archivo.

## 4. Convenciones de tablas y columnas

| Elemento | Convención | Ejemplo |
|---|---|---|
| Tabla | snake_case, en **singular**, en minúsculas | `comunidad`, `miembro_comunidad` |
| Columna | snake_case, en minúsculas | `fecha_nacimiento`, `contacto_responsable` |
| Clave primaria | `id BIGINT AUTO_INCREMENT PRIMARY KEY` | `id` |
| Clave foránea (columna) | `<tabla_referida>_id` | `comunidad_id` |
| Clave foránea (restricción) | `fk_<tabla>_<tabla_referida>` | `fk_miembro_comunidad` |
| Restricción única | `uk_<tabla>_<columna>` | `uk_comunidad_nombre` |
| Índice | `idx_<tabla>_<columna>` | `idx_actividad_fecha` |
| Tabla intermedia (muchos a muchos) | `<tabla1>_<tabla2>` | `huerto_planta` |

**Nombres siempre en minúsculas:** dentro del contenedor, MariaDB distingue mayúsculas en los nombres de tabla. `Comunidad` y `comunidad` son tablas distintas.

**Tipos de dato recomendados:**

| Dato | Tipo en MariaDB | Tipo en Java |
|---|---|---|
| Identificador | `BIGINT` | `Long` |
| Texto corto (nombre, correo) | `VARCHAR(n)` | `String` |
| Texto largo (descripción, uso medicinal) | `TEXT` | `String` |
| Sí / no | `BOOLEAN` | `boolean` |
| Fecha sin hora (siembra, nacimiento) | `DATE` | `LocalDate` |
| Fecha y hora (registro de actividad) | `DATETIME` | `LocalDateTime` |
| Número con decimales (área, latitud) | `DECIMAL(p, s)` | `BigDecimal` |
| Valor de una lista fija (tipo de actividad) | `VARCHAR(30)` | `enum` con `@Enumerated(EnumType.STRING)` |

Las enumeraciones se guardan como texto, nunca como número (`EnumType.ORDINAL`): si alguien reordena el `enum`, los números guardados pasarían a significar otra cosa.

**Columnas de auditoría** recomendadas en las tablas que registran acciones de usuarios:

```sql
creado_en      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

**Juego de caracteres:** la base usa `utf8mb4` por defecto, así que tildes, ñ y nombres en kichwa se guardan bien sin configurar nada en cada tabla.

## 5. De la tabla a la entidad

Spring Boot traduce automáticamente los nombres de Java a snake_case: el campo `contactoResponsable` se mapea a la columna `contacto_responsable`. Por eso no hace falta `@Column(name = ...)` si se respeta la convención.

> El SQL de ejemplo es solo ilustrativo. Las tablas reales salen del diagrama de clases validado.

`V1__crear_tabla_comunidad.sql`

```sql
CREATE TABLE comunidad (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre               VARCHAR(120) NOT NULL,
    contacto_responsable VARCHAR(120) NOT NULL,
    creado_en            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_comunidad_nombre UNIQUE (nombre)
);
```

`V2__crear_tabla_miembro.sql`

```sql
CREATE TABLE miembro (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id     BIGINT       NOT NULL,
    nombre           VARCHAR(120) NOT NULL,
    fecha_nacimiento DATE         NOT NULL,
    nivel            VARCHAR(20)  NOT NULL DEFAULT 'APRENDIZ',
    CONSTRAINT fk_miembro_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidad (id)
);
```

La entidad `Miembro` correspondiente:

```java
@Entity
@Table(name = "miembro")
@Getter
@Setter
@NoArgsConstructor
public class Miembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Comunidad comunidad;          // columna comunidad_id

    private String nombre;

    private LocalDate fechaNacimiento;    // columna fecha_nacimiento

    @Enumerated(EnumType.STRING)
    private Nivel nivel;                  // APRENDIZ, OFICIAL, MAESTRO
}
```

## 6. Datos de catálogo

Los catálogos que el sistema necesita desde el inicio (tipos de suelo, de agua de riego, de abono, de cerca, diseños de huerto y las plantas iniciales de los lineamientos del MSP) se cargan con migraciones de `INSERT`, separadas de las que crean la tabla:

```
V7__crear_tabla_tipo_suelo.sql
V8__insertar_catalogo_tipo_suelo.sql
```

Así cualquier integrante que levante la base tiene los mismos datos de catálogo, y las pruebas de integración también los tienen.

Los datos de prueba (comunidades o usuarios ficticios para desarrollar) **no** van en migraciones, porque terminarían en producción. Se crean desde la aplicación o desde las pruebas.
