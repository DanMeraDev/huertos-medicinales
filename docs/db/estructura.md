# Estructura de la base de datos

La base es **MariaDB 11.4**. Su esquema lo controla únicamente **Flyway**: cada cambio es un archivo SQL versionado en el repo. Este documento explica dónde van las migraciones, cómo se nombran y qué convenciones siguen las tablas.

Relacionados: [Ejecución de la base](ejecucion.md) · [Errores comunes de la base](errores-comunes.md) · [Estructura del back](../backend/estructura.md)

## 1. Dónde está cada cosa

```
backend/src/main/resources/
└── db/migration/
    ├── V1__crear_usuarios_y_solicitudes.sql
    ├── V2__crear_comunidades_y_miembros.sql
    ├── V3__crear_catalogo_plantas.sql
    ├── V4__crear_huertos_bodega_y_siembras.sql
    ├── V5__crear_actividades_y_talentos_comunidad.sql
    ├── V6__crear_saberes_y_actividades_msp.sql
    ├── V7__crear_insignias_de_miembros.sql
    └── ...
```

| Pieza | Dónde | Quién la controla |
|---|---|---|
| Tablas, columnas, índices, claves | `db/migration/*.sql` | Flyway, al arrancar el back |
| Datos de catálogo (plantas iniciales del MSP, saberes ancestrales, calendario lunar) | `db/migration/*.sql`, como `INSERT` | Flyway |
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
| `V8__insertar_plantas_msp.sql` | `V8_insertar_plantas_msp.sql` | Un solo guion bajo |
| `V9__agregar_columna_area_a_subhuertos.sql` | `v9__subhuertos.sql` | Minúscula y descripción vaga |
| `V10__insertar_saberes_ancestrales.sql` | `V10__insertar saberes.sql` | Espacio y tilde |

**Si dos personas usan el mismo número:** el CI falla con `Found more than one migration with version`. Quien fusiona después renombra su archivo al siguiente número libre, antes de fusionar.

**Una migración, un propósito:** crear una tabla, agregar una columna o cargar un catálogo. No se mezclan cambios de varios casos de uso en el mismo archivo.

## 4. Convenciones de tablas y columnas

| Elemento | Convención | Ejemplo |
|---|---|---|
| Tabla | snake_case, en **plural**, en minúsculas | `comunidades`, `miembros` |
| Columna | snake_case, en minúsculas | `fecha_nacimiento`, `contacto_responsable` |
| Clave primaria | `id BIGINT AUTO_INCREMENT PRIMARY KEY` | `id` |
| Clave foránea (columna) | `<tabla_referida>_id` | `comunidad_id` |
| Clave foránea (restricción) | `fk_<tabla>_<referencia>` | `fk_comunidad_miembros_comunidad` |
| Restricción única | `uk_<tabla>_<columna>` | `uk_comunidades_nombre` |
| Restricción de validación | `ck_<tabla>_<columna>` | `ck_talentos_comunidad_meta` |
| Índice | `idx_<tabla>_<columna>` | `idx_siembras_fecha_siembra` |
| Tabla intermedia (muchos a muchos) | `<tabla1_singular>_<tabla2>` | `comunidad_miembros` |

Cada tabla termina con `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`. Esa intercalación no distingue mayúsculas ni tildes, así que una búsqueda con `LIKE` encuentra `Manzanilla` al buscar `manzanilla`.

**Nombres siempre en minúsculas:** dentro del contenedor, MariaDB distingue mayúsculas en los nombres de tabla. `Comunidades` y `comunidades` son tablas distintas.

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

**Columna de auditoría** en las tablas que registran acciones de usuarios:

```sql
creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
```

En la entidad se mapea con `@CreationTimestamp` y `@Column(updatable = false)`.

**Juego de caracteres:** la base usa `utf8mb4` por defecto, así que tildes, ñ y nombres en kichwa se guardan bien sin configurar nada en cada tabla.

## 5. De la tabla a la entidad

Spring Boot traduce automáticamente los nombres de Java a snake_case: el campo `contactoResponsable` se mapea a la columna `contacto_responsable`. Por eso no hace falta `@Column(name = ...)` si se respeta la convención.

Ejemplo real, de `V2__crear_comunidades_y_miembros.sql`: la tabla intermedia que une comunidades y miembros.

```sql
CREATE TABLE comunidad_miembros (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    comunidad_id        BIGINT       NOT NULL,
    miembro_id          BIGINT       NOT NULL,
    activo_en_comunidad BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_ingreso       DATE         NOT NULL,
    fecha_baja          DATE         NULL,
    motivo_baja         VARCHAR(255) NULL,
    CONSTRAINT uk_comunidad_miembros_comunidad_miembro UNIQUE (comunidad_id, miembro_id),
    CONSTRAINT fk_comunidad_miembros_comunidad FOREIGN KEY (comunidad_id) REFERENCES comunidades (id),
    CONSTRAINT fk_comunidad_miembros_miembro FOREIGN KEY (miembro_id) REFERENCES miembros (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

La entidad `ComunidadMiembro` correspondiente:

```java
@Entity
@Table(name = "comunidad_miembros")
@Getter
@Setter
@NoArgsConstructor
public class ComunidadMiembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Comunidad comunidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Miembro miembro;

    private boolean activoEnComunidad;

    private LocalDate fechaIngreso;

    private LocalDate fechaBaja;

    private String motivoBaja;
}
```

El campo `comunidad` se mapea a la columna `comunidad_id` y `activoEnComunidad` a `activo_en_comunidad`, sin `@Column(name = ...)`.

## 6. Datos de catálogo

Las listas fijas (tipos de suelo, de agua de riego, de abono, de cerca, diseños de huerto, fases lunares) no son tablas: son `enum` de Java guardados como `VARCHAR(30)`.

Los datos que el sistema necesita desde el inicio (las plantas de los lineamientos del MSP, los saberes ancestrales y el calendario lunar) se cargan con migraciones de `INSERT`, separadas de las que crean la tabla:

```
V3__crear_catalogo_plantas.sql
V8__insertar_plantas_msp.sql
```

Así cualquier integrante que levante la base tiene los mismos datos de catálogo, y las pruebas de integración también los tienen.

Los datos de prueba (comunidades o usuarios ficticios para desarrollar) **no** van en migraciones, porque terminarían en producción. Se crean desde la aplicación o desde las pruebas.
