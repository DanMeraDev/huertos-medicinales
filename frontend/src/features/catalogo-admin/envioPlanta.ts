import { ApiError } from '../../api/client'
import { mensajeErrorFotografia, subirFotografiaPlanta } from './plantasAdminApi'
import type { ErroresPlanta } from './validacionPlanta'

export interface EnlaceAviso {
  ruta: string
  texto: string
}

export interface Aviso {
  mensaje: string
  enlace: EnlaceAviso | null
}

export const ENLACE_CATALOGO: EnlaceAviso = { ruta: '/catalogo-plantas', texto: 'Volver al catálogo' }

export const AVISO_SESION: Aviso = {
  mensaje: 'Necesitas iniciar sesión como Técnico MSP',
  enlace: { ruta: '/login', texto: 'Iniciar sesión' },
}

export const CAMPOS_POR_SECCION: (keyof ErroresPlanta)[][] = [
  ['nombreComun', 'nombreCientifico', 'nombreKichwa', 'familiaBotanica', 'fotografiaUrl'],
  ['cualidadTermica', 'cicloCultivo', 'origen', 'altitudMinMsnm', 'altitudMaxMsnm', 'rangoAltitud',
    'regionesDistribucion'],
  ['categorias', 'usosMedicinales', 'preparacionDosificacion', 'contraindicaciones'],
]

export function seccionConError(errores: ErroresPlanta) {
  return CAMPOS_POR_SECCION.findIndex((campos) => campos.some((campo) => errores[campo]))
}

export function esErrorDeEstado(error: unknown, ...estados: number[]): error is ApiError {
  return error instanceof ApiError && estados.includes(error.status)
}

export function erroresDelServidor(error: unknown, mensajeDuplicado: string): ErroresPlanta | null {
  if (esErrorDeEstado(error, 409)) return { nombreCientifico: mensajeDuplicado }
  if (esErrorDeEstado(error, 400)) return error.errores ?? { rangoAltitud: error.detalle ?? error.message }
  return null
}

export async function enviarConFotografia(
  fotografia: File | null,
  fotografiaActual: string | null,
  enviar: (fotografiaUrl: string | null) => Promise<void>,
  alFallarFotografia: (aviso: Aviso) => void,
) {
  let fotografiaUrl: string | null
  try {
    fotografiaUrl = fotografia ? (await subirFotografiaPlanta(fotografia)).fotografiaUrl : fotografiaActual
  } catch (error) {
    alFallarFotografia({ mensaje: mensajeErrorFotografia(error), enlace: null })
    return
  }
  await enviar(fotografiaUrl)
}
