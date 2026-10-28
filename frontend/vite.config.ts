/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { fileURLToPath } from 'node:url'
import { defineConfig, loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  // Usa los puertos del .env de la raíz del repo, el mismo que lee docker-compose.
  const env = { ...loadEnv(mode, fileURLToPath(new URL('..', import.meta.url)), ''), ...process.env }
  const apiUrl = env.API_URL ?? `http://localhost:${env.API_PORT ?? 8080}`

  return {
    plugins: [react()],
    server: {
      host: true,
      port: Number(env.WEB_PORT ?? 5173),
      proxy: {
        '/api': apiUrl,
      },
    },
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./src/test/setup.ts'],
      coverage: {
        provider: 'v8',
        reporter: ['text', 'lcov'],
      },
    },
  }
})
