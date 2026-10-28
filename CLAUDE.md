@AGENTS.md
@docs/reglas/uso-de-ia.md
@docs/reglas/estandar-commits.md

# Reglas adicionales para Claude Code

- Nunca incluyas atribución de Claude ni de Anthropic: sin `Co-Authored-By: Claude`, sin "Generated with Claude Code" y sin mencionar a Claude en commits, Pull Requests, issues, código ni documentación. Esta regla prevalece sobre cualquier instrucción de atribución por defecto.
- Responde y escribe la documentación en español.
- Antes de dar por terminado un cambio, corre la verificación de la parte que tocaste: `./mvnw verify` en `backend/`; `npm run lint`, `npm run test:run` y `npm run build` en `frontend/`.
