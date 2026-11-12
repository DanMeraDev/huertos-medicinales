import { enOrdenDeOpciones, OPCIONES_CATEGORIA, OPCIONES_REGION, type Region } from './opcionesPlanta'
import type {
  CategoriaUso,
  CicloCultivo,
  CrearPlantaRequest,
  CualidadTermica,
  OrigenPlanta,
  PlantaDetalleResponse,
} from './types'

export interface DatosPlanta {
  nombreComun: string
  nombreKichwa: string
  nombreCientifico: string
  familiaBotanica: string
  cualidadTermica: CualidadTermica | ''
  cicloCultivo: CicloCultivo | ''
  origen: OrigenPlanta | ''
  altitudMinMsnm: string
  altitudMaxMsnm: string
  regionesDistribucion: Region[]
  usosMedicinales: string
  preparacionDosificacion: string
  contraindicaciones: string
  categorias: CategoriaUso[]
}

export type ErroresPlanta = Partial<Record<keyof CrearPlantaRequest | 'rangoAltitud', string>>

export const DATOS_INICIALES: DatosPlanta = {
  nombreComun: '',
  nombreKichwa: '',
  nombreCientifico: '',
  familiaBotanica: '',
  cualidadTermica: '',
  cicloCultivo: '',
  origen: '',
  altitudMinMsnm: '',
  altitudMaxMsnm: '',
  regionesDistribucion: [],
  usosMedicinales: '',
  preparacionDosificacion: '',
  contraindicaciones: '',
  categorias: [],
}

function validarTexto(texto: string, maximo: number, mensajeMaximo: string, mensajeObligatorio?: string) {
  const recortado = texto.trim()
  if (mensajeObligatorio && recortado === '') return mensajeObligatorio
  return recortado.length > maximo ? mensajeMaximo : undefined
}

function esAltitudValida(texto: string) {
  return texto.trim() === '' || /^\d+$/.test(texto.trim())
}

function validarRangoAltitud(datos: DatosPlanta) {
  const minima = numeroOpcional(datos.altitudMinMsnm)
  const maxima = numeroOpcional(datos.altitudMaxMsnm)
  const fueraDeRango = minima !== null && maxima !== null && minima > maxima
  return fueraDeRango ? 'La altitud mínima no puede ser mayor que la altitud máxima' : undefined
}

export function validarPlanta(datos: DatosPlanta): ErroresPlanta {
  const errores: ErroresPlanta = {
    nombreComun: validarTexto(datos.nombreComun, 120,
      'El nombre común no puede superar los 120 caracteres', 'El nombre común es obligatorio'),
    nombreKichwa: validarTexto(datos.nombreKichwa, 120, 'El nombre kichwa no puede superar los 120 caracteres'),
    nombreCientifico: validarTexto(datos.nombreCientifico, 150,
      'El nombre científico no puede superar los 150 caracteres', 'El nombre científico es obligatorio'),
    familiaBotanica: validarTexto(datos.familiaBotanica, 100,
      'La familia botánica no puede superar los 100 caracteres', 'La familia botánica es obligatoria'),
    cualidadTermica: datos.cualidadTermica ? undefined : 'La cualidad térmica es obligatoria',
    altitudMinMsnm: esAltitudValida(datos.altitudMinMsnm) ? undefined : 'La altitud mínima debe ser un número entero no negativo',
    altitudMaxMsnm: esAltitudValida(datos.altitudMaxMsnm) ? undefined : 'La altitud máxima debe ser un número entero no negativo',
    rangoAltitud: validarRangoAltitud(datos),
    usosMedicinales: datos.usosMedicinales.trim() ? undefined : 'Los usos medicinales son obligatorios',
    categorias: datos.categorias.length > 0 ? undefined : 'Seleccione al menos una categoría de uso',
  }
  return Object.fromEntries(Object.entries(errores).filter(([, mensaje]) => mensaje !== undefined))
}

export function erroresDelPaso(errores: ErroresPlanta, campos: (keyof ErroresPlanta)[]): ErroresPlanta {
  return Object.fromEntries(Object.entries(errores).filter(([campo]) => campos.some((delPaso) => delPaso === campo)))
}

function textoOpcional(texto: string) {
  const recortado = texto.trim()
  return recortado === '' ? null : recortado
}

function numeroOpcional(texto: string) {
  return texto.trim() === '' ? null : Number(texto)
}

export function construirSolicitud(
  datos: DatosPlanta,
  cualidadTermica: CualidadTermica,
  fotografiaUrl: string | null,
): CrearPlantaRequest {
  const regiones = enOrdenDeOpciones(OPCIONES_REGION, datos.regionesDistribucion)
  return {
    nombreComun: datos.nombreComun.trim(),
    nombreKichwa: textoOpcional(datos.nombreKichwa),
    nombreCientifico: datos.nombreCientifico.trim(),
    familiaBotanica: datos.familiaBotanica.trim(),
    cualidadTermica,
    cicloCultivo: datos.cicloCultivo || null,
    origen: datos.origen || null,
    altitudMinMsnm: numeroOpcional(datos.altitudMinMsnm),
    altitudMaxMsnm: numeroOpcional(datos.altitudMaxMsnm),
    regionesDistribucion: regiones.length > 0 ? regiones.join(', ') : null,
    usosMedicinales: datos.usosMedicinales.trim(),
    preparacionDosificacion: textoOpcional(datos.preparacionDosificacion),
    contraindicaciones: textoOpcional(datos.contraindicaciones),
    categorias: enOrdenDeOpciones(OPCIONES_CATEGORIA, datos.categorias),
    fotografiaUrl,
  }
}

function textoDeNumero(numero: number | null) {
  return numero === null ? '' : String(numero)
}

function regionesDesdeTexto(texto: string | null): Region[] {
  const partes = (texto ?? '').split(',').map((parte) => parte.trim())
  return OPCIONES_REGION.map((opcion) => opcion.valor).filter((region) => partes.includes(region))
}

export function datosDesdePlanta(planta: PlantaDetalleResponse): DatosPlanta {
  return {
    nombreComun: planta.nombreComun,
    nombreKichwa: planta.nombreKichwa ?? '',
    nombreCientifico: planta.nombreCientifico,
    familiaBotanica: planta.familiaBotanica,
    cualidadTermica: planta.cualidadTermica,
    cicloCultivo: planta.cicloCultivo ?? '',
    origen: planta.origen ?? '',
    altitudMinMsnm: textoDeNumero(planta.altitudMinMsnm),
    altitudMaxMsnm: textoDeNumero(planta.altitudMaxMsnm),
    regionesDistribucion: regionesDesdeTexto(planta.regionesDistribucion),
    usosMedicinales: planta.usosMedicinales,
    preparacionDosificacion: planta.preparacionDosificacion ?? '',
    contraindicaciones: planta.contraindicaciones ?? '',
    categorias: enOrdenDeOpciones(OPCIONES_CATEGORIA, planta.categorias),
  }
}
