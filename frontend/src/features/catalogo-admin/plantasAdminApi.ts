import type { FotografiaSubidaResponse } from './types'

export function subirFotografiaPlanta(_archivo: File): Promise<FotografiaSubidaResponse> {
  return Promise.resolve({ fotografiaUrl: '' })
}

export function mensajeErrorFotografia(_error: unknown): string {
  return ''
}
