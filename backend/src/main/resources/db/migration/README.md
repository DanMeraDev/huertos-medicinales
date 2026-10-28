# Migraciones Flyway

Cada cambio al esquema es un archivo nuevo en esta carpeta. Nunca se edita una migración que ya está en `develop`.

Formato del nombre: `V<número>__<descripcion_en_snake_case>.sql`, por ejemplo `V1__crear_tabla_comunidad.sql`.

Antes de crear una migración, revisa en `develop` cuál es el último número para no repetirlo.
