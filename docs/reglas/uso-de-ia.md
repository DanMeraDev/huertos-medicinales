# Uso de IA para escribir código

Se puede usar IA (ChatGPT, Copilot, Claude, Gemini, Cursor u otra) para escribir código del back y del front. Este documento define cómo debe quedar ese código. Las reglas aplican igual si el código lo escribe una persona, pero la IA tiende a romperlas con más frecuencia: agrega comentarios obvios, código "por si acaso", dependencias que nadie pidió y funciones que nunca se llaman.

Relacionados: [Estructura del back](../backend/estructura.md) · [Estructura del front](../frontend/estructura.md) · [Estructura de la base](../db/estructura.md) · [Flujo de trabajo con Git](flujo-de-trabajo-git.md)

## 1. Principio

**Quien hace el commit es el autor del código, lo haya escrito o no la IA.** Debe poder explicar cada línea en la revisión del Pull Request. Si no entiende una parte, no la sube.

### Sin firmas de la IA

Ningún commit, Pull Request, issue, archivo de código ni documento lleva firmas o menciones de la herramienta usada: ni `Co-Authored-By`, ni "Generated with ...", ni el nombre del asistente. Varios asistentes las agregan solos; se borran antes del commit. El autor es la persona.

## 2. Reglas para todo el código

### Solo lo necesario

- Se escribe **únicamente lo que pide el issue**. Nada de funcionalidades extra, parámetros "por si acaso", validaciones que no están en los criterios de aceptación ni pantallas que nadie pidió.
- Se **reutiliza lo que ya existe**: el cliente `api/client.ts`, `GlobalExceptionHandler`, los componentes de `components/`. No se crea una segunda versión de algo que ya está.
- **No se agregan dependencias** (`pom.xml`, `package.json`) sin acordarlo antes en el issue. La IA suele resolver todo instalando una librería.
- **No se cambian archivos que el issue no toca.** Si la IA reformatea, renombra o "mejora" otros archivos, esos cambios se descartan.

### Nada de código sin usar

- **Sin código muerto:** ni imports, variables, parámetros, métodos o clases que no se usan, ni ramas `if` que nunca se ejecutan.
- **Sin código comentado.** Si algo ya no sirve, se borra. Git guarda el historial.
- **Sin rastros de depuración:** ni `System.out.println`, ni `printStackTrace`, ni `console.log`.
- **Sin marcadores pendientes:** ni `TODO`, ni `FIXME`, ni código de relleno, ni datos inventados, ni métodos vacíos "para después".

### Sin comentarios

- **No se escriben comentarios dentro del código.** El código se explica con buenos nombres de clases, métodos y variables. Si hace falta un comentario para entender una línea, se reescribe la línea.
- La IA agrega comentarios como `// Obtiene la comunidad por id` o `// Guardar en la base`. **Se borran todos** antes del commit.
- **Única excepción: el Javadoc** que exige el estándar de documentación, es decir, las interfaces de `service` y las reglas de negocio que no son obvias por su nombre (cálculo de puntos, paso de nivel, fase lunar). Los endpoints se documentan con anotaciones de Swagger, no con comentarios.

### Calidad

- **Nombres del negocio en español**, como en el resto del proyecto: `registrarComunidad`, no `registerCommunity` ni `doStuff`.
- **Funciones cortas, con una sola responsabilidad.** Si la IA genera un método de 80 líneas, se divide.
- **Sin duplicar:** si el mismo bloque aparece dos veces, se extrae.
- **Sin valores fijos** de URLs, puertos, claves ni contraseñas en el código. Van en la [configuración](../backend/estructura.md#6-configuración-applicationyml) o en el `.env`.
- **Errores visibles:** nada de `catch` vacíos ni de `catch (Exception e)` que oculten el problema.

## 3. Reglas específicas del back

- **Respetar las capas:** el controller no usa repositorios, el servicio no devuelve entidades a la API y las entidades no se exponen. Ver [estructura del back](../backend/estructura.md).
- **Inyección por constructor** con `@RequiredArgsConstructor` y campos `private final`. Nada de `@Autowired` sobre campos.
- **DTOs como `record`.** Sin `@Data` en entidades.
- **`@Transactional` en el servicio**, no en el controller.
- **Errores con las excepciones de `exception/`**, convertidas por `GlobalExceptionHandler`. El controller no arma respuestas de error a mano.
- **Esquema solo con migraciones Flyway nuevas.** La IA suele proponer `ddl-auto: update` o editar una migración existente: **no se acepta**. Ver [estructura de la base](../db/estructura.md).
- **Configuración en `application.yml`.** Si la IA crea un `application.properties`, se descarta.
- **Para registrar eventos**, el logger de Lombok (`@Slf4j`), nunca `System.out`.

## 4. Reglas específicas del front

- **Sin `any`.** Los tipos de las respuestas del API son los mismos de los DTOs del back.
- **Llamadas al back solo desde `api/` y los archivos `*Api.ts` de cada feature.** Ningún componente usa `fetch` ni `axios` directamente.
- **Respetar las carpetas:** una feature no importa archivos de otra. Ver [estructura del front](../frontend/estructura.md).
- **Componentes como funciones**, con exportación con nombre.
- **Sin estado ni efectos que no se usan:** cada `useState` y `useEffect` tiene que hacer falta.
- **Sin librerías de interfaz nuevas** (Material UI, Bootstrap, Tailwind, etc.) sin acordarlo antes.

## 5. Pruebas escritas con IA

- **TDD también con IA:** primero se escribe la prueba del criterio de aceptación, se comprueba que falla y después se pide el código.
- **Si una prueba falla, se corrige el código, no la prueba.** No se acepta que la IA cambie lo esperado (`assertEquals`, `expect`) para que la prueba pase.
- **Las pruebas comprueban comportamiento real.** No se acepta una prueba que solo verifica que se llamó a un mock, ni una que simula justo lo que está probando.
- **Sin pruebas vacías** ni `@Disabled` o `it.skip` para que el CI quede en verde.

## 6. Cómo pedirle código a la IA

1. **Dale contexto:**
   - El texto del issue, con sus criterios de aceptación.
   - Los archivos relacionados del repo.
   - Este documento, o el bloque de la sección 7.
2. **Pide el cambio mínimo:** "solo lo necesario para este criterio de aceptación, sin comentarios, sin código adicional".
3. **Pide un archivo o una capa a la vez.** Las respuestas largas son las que más basura traen.
4. **Revisa la respuesta con la lista de la sección 8** antes de pegarla en el proyecto.

Los asistentes que trabajan dentro del repo leen solos el archivo `AGENTS.md` de la raíz, que resume estas reglas: Codex, Cursor y el modo agente de Copilot lo leen directamente, y Claude Code a través de `CLAUDE.md`, que lo incluye. En ChatGPT, Gemini o cualquier chat web, hay que pegar el bloque de la sección 7 al inicio de la conversación.

## 7. Instrucciones para pegar en el chat de la IA

```
Proyecto: Huertos Medicinales Comunitarios.
Back: Java 21, Spring Boot 4.1, Spring Data JPA, Flyway, MapStruct, Lombok, MariaDB 11.4.
Front: React 19, TypeScript, Vite.

Paquetes del back (com.huertos): controller, service, service/impl, repository,
model, dto, mapper, config, exception.
Carpetas del front (src): api, features/<feature>, components, pages, test.

Reglas obligatorias para el código que generes:
- Escribe solo lo necesario para lo que te pido. Nada de funcionalidades,
  parámetros o validaciones adicionales.
- No escribas comentarios en el código. Solo Javadoc en interfaces de service
  si te lo pido.
- No dejes código sin usar: imports, variables, métodos, código comentado,
  TODO, console.log ni System.out.
- No agregues dependencias nuevas ni cambies archivos que no te pedí.
- Nombres del negocio en español.
- Back: inyección por constructor con @RequiredArgsConstructor, DTOs como
  record, sin @Data en entidades, el controller no accede al repository,
  errores con excepciones manejadas por @RestControllerAdvice, cambios de
  esquema solo con una migración Flyway nueva, configuración en
  application.yml (nunca application.properties).
- Front: sin any, llamadas al back solo desde los archivos *Api.ts usando
  api/client.ts, componentes como funciones con exportación con nombre.
- Si una prueba falla, corrige el código, no la prueba.
- No agregues firmas ni menciones tuyas en commits, PRs ni código.
```

## 8. Revisión antes del commit

Antes de hacer commit de código generado con IA, se revisa:

- [ ] Entiendo cada línea y podría explicarla en la revisión.
- [ ] El commit no tiene `Co-Authored-By` ni menciones de la IA.
- [ ] Solo cambia lo que pide el issue; descarté los cambios en otros archivos.
- [ ] No hay comentarios, salvo el Javadoc que exige el estándar.
- [ ] No hay código sin usar, código comentado, `TODO`, `console.log` ni `System.out`.
- [ ] No agregó dependencias ni un `application.properties`.
- [ ] Respeta la estructura de carpetas y las capas.
- [ ] Las pruebas pasan sin haber cambiado lo que esperan.
- [ ] En el front: `npm run lint`, `npm run test:run` y `npm run build` pasan.
- [ ] En el back: `.\mvnw.cmd verify` pasa.

**Lo que se detecta solo:**
- **Front:** TypeScript hace fallar el `build` si queda una variable o un parámetro sin usar, y `npm run lint` también lo avisa.
- **Back:** no hay un chequeo automático equivalente. Lo marcan en gris IntelliJ y VS Code, así que la revisión es manual. Antes del commit, en IntelliJ, *Code → Optimize Imports* (`Ctrl+Alt+O`) elimina los imports sin usar.
